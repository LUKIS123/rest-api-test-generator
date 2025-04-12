package pl.edu.pwr.compiler;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import pl.edu.pwr.grammar.TestGeneratorBaseVisitor;
import pl.edu.pwr.grammar.TestGeneratorParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class GeneratorCommandVisitor extends TestGeneratorBaseVisitor<ST> {
    private final STGroup stGroup;
    private static final Map<String, String> templateNameMap = Map.of(
            "GET", "getRequest",
            "POST", "postRequest",
            "PUT", "putRequest",
            "PATCH", "patchRequest",
            "DELETE", "deleteRequest");
    private static final String defaultPackageName = "pl.edu.pwr";

    public GeneratorCommandVisitor(STGroup stGroup) {
        super();
        this.stGroup = stGroup;
    }

    @Override
    protected ST defaultResult() {
        return stGroup.getInstanceOf("deflt");
    }

    @Override
    protected ST aggregateResult(ST aggregate, ST nextResult) {
        if (nextResult != null) {
            aggregate.add("elem", nextResult);
        }

        return aggregate;
    }

    @Override
    public ST visitTerminal(TerminalNode node) {
        String nodeText = node.getText();
        if (nodeText.equals("<EOF>")) {
            return null;
        }

        return new ST("Terminal node:<n>").add("n", nodeText);
    }

    @Override
    public ST visitClassDef(TestGeneratorParser.ClassDefContext ctx) {
        ST combinedTestTemplate = stGroup.getInstanceOf("combinedTemplate");

        String testClass = ctx.CLASS_NAME().getText();
        String[] testClassNameSplit = testClass.split("\\.");
        String className = testClassNameSplit[testClassNameSplit.length - 1];
        String packageName = defaultPackageName;

        try {

            if (testClassNameSplit.length > 1) {
                StringBuilder packageNameBuilder = new StringBuilder();
                for (int i = 0; i < testClassNameSplit.length - 1; i++) {
                    packageNameBuilder.append(testClassNameSplit[i]);
                    if (i < testClassNameSplit.length - 2) {
                        packageNameBuilder.append(".");
                    }
                }
                packageName = packageNameBuilder.toString();
            }

            List<TestGeneratorParser.TestContext> testContexts = ctx.test();
            for (TestGeneratorParser.TestContext testContext : testContexts) {
                ST testMethodSt = visit(testContext);
                combinedTestTemplate.add("content", testMethodSt);
            }

            if (ctx.baseUrl() != null) {
                ST baseUrlSt = visit(ctx.baseUrl());
                return stGroup.getInstanceOf("initTestClassWithBaseConfig")
                        .add("package", packageName)
                        .add("className", className)
                        .add("baseConfig", baseUrlSt)
                        .add("content", combinedTestTemplate);
            }
        } catch (Exception e) {
            System.err.println("Error in class definition: " + e.getMessage());
        }

        return stGroup.getInstanceOf("initTestClass")
                .add("package", packageName)
                .add("className", className)
                .add("content", combinedTestTemplate);
    }

    @Override
    public ST visitBaseUrl(TestGeneratorParser.BaseUrlContext ctx) {
        ST baseUrlSt = stGroup.getInstanceOf("combinedTemplate");
        String url = ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1);
        try {
            URI uri = new URI(url);

            String baseURI = uri.getScheme() + "://" + uri.getHost();
            ST classBaseUriSt = stGroup.getInstanceOf("classBaseUri")
                    .add("baseUri", baseURI);
            baseUrlSt.add("content", classBaseUriSt);

            if (uri.getPort() != -1) {
                int port = uri.getPort();
                ST classPortSt = stGroup.getInstanceOf("classPort")
                        .add("port", port);
                baseUrlSt.add("content", classPortSt);
            }

            if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                String basePath = uri.getPath();
                ST classBasePathSt = stGroup.getInstanceOf("classBasePath")
                        .add("basePath", basePath);
                baseUrlSt.add("content", classBasePathSt);
            }

            return baseUrlSt;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + url, e);
        }
    }

    @Override
    public ST visitTest(TestGeneratorParser.TestContext ctx) {
        String testMethodName = ctx.NAME().getText();
        ST testMethodSt = stGroup.getInstanceOf("initTestMethod");
        testMethodSt.add("name", testMethodName);

        try {
            ST requestSt = visit(ctx.request());
            testMethodSt.add("request", requestSt);

            ST assertSt = visit(ctx.validate());
            testMethodSt.add("assert", assertSt);
        } catch (Exception e) {
            System.err.println("Error in test method: " + e.getMessage());
        }

        return testMethodSt;
    }

    @Override
    public ST visitRequest(TestGeneratorParser.RequestContext ctx) {
        ST requestSt = stGroup.getInstanceOf("combinedTemplate");
        try {
            List<TestGeneratorParser.RequestElementContext> requestElementContexts = ctx.requestElement();
            for (TestGeneratorParser.RequestElementContext requestElementContext : requestElementContexts) {
                ST elementSt = visit(requestElementContext);
                if (elementSt == null) {
                    continue;
                }
                requestSt.add("content", elementSt);
            }

            ST methodSt = visit(ctx.method());
            requestSt.add("content", methodSt);
        } catch (Exception e) {
            System.err.println("Error in request: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return requestSt;
    }

    @Override
    public ST visitRequestElement(TestGeneratorParser.RequestElementContext ctx) {
        try {
            if (ctx.url() != null) {
                return visit(ctx.url());
            } else if (ctx.headers() != null) {
                return visit(ctx.headers());
            } else if (ctx.body() != null) {
                return visit(ctx.body());
            } else if (ctx.queryParams() != null) {
                return visit(ctx.queryParams());
            }
        } catch (Exception e) {
            System.err.println("Error in request: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return null;
    }

    @Override
    public ST visitMethod(TestGeneratorParser.MethodContext ctx) {
        String templateName = templateNameMap.get(ctx.HTTP_METHOD().getText());
        if (templateName == null) {
            throw new IllegalArgumentException("Unknown method: " + ctx.HTTP_METHOD().getText());
        }

        ST method = stGroup.getInstanceOf(templateName);

        TerminalNode endpointCall = ctx.STRING();
        if (endpointCall != null) {
            String endpointCallText = endpointCall.getText();
            if (endpointCallText != null && !endpointCallText.isBlank()) {
                method.add("path", endpointCall);
            }
        }

        return method;
    }

    @Override
    public ST visitUrl(TestGeneratorParser.UrlContext ctx) {
        String url = ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1);
        try {
            URI uri = new URI(url);

            String baseURI = uri.getScheme() + "://" + uri.getHost();
            Integer port = null;
            String basePath = null;

            if (uri.getPort() != -1) {
                port = uri.getPort();
            }

            if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                basePath = uri.getPath();
            }

            if (port != null) {
                ST requestAddressWithPortSt = stGroup.getInstanceOf("requestWithPortAndPath");
                requestAddressWithPortSt.add("uri", baseURI).add("port", port);
                if (basePath != null) {
                    requestAddressWithPortSt.add("path", basePath);
                }
                return requestAddressWithPortSt;
            }

            if (basePath != null) {
                ST requestAddressSt = stGroup.getInstanceOf("requestWithPath");
                requestAddressSt.add("uri", baseURI).add("path", basePath);
                return requestAddressSt;
            }

            ST requestSt = stGroup.getInstanceOf("request");
            requestSt.add("uri", baseURI);
            return requestSt;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + url, e);
        }
    }

    @Override
    public ST visitQueryParams(TestGeneratorParser.QueryParamsContext ctx) {
        ST combinedTemplate = stGroup.getInstanceOf("combinedTemplate");

        List<TestGeneratorParser.QueryParamContext> queryParamContexts = ctx.queryParam();
        for (TestGeneratorParser.QueryParamContext queryParamContext : queryParamContexts) {
            ST queryParamSt = visit(queryParamContext);
            combinedTemplate.add("content", queryParamSt);
        }

        return combinedTemplate;
    }

    @Override
    public ST visitQueryParam(TestGeneratorParser.QueryParamContext ctx) {
        ST queryParamSt = stGroup.getInstanceOf("requestQueryParams");
        List<TerminalNode> keyValuePairs = ctx.STRING();

        String key = keyValuePairs.get(0).getText();
        queryParamSt.add("queryKey", key.substring(1, key.length() - 1));

        String value = keyValuePairs.get(1).getText();
        queryParamSt.add("value", Helper.getValidJsonValue(value));

        return queryParamSt;
    }

    @Override
    public ST visitHeaders(TestGeneratorParser.HeadersContext ctx) {
        ST combinedTemplate = stGroup.getInstanceOf("combinedTemplate");

        List<TestGeneratorParser.HeaderContext> headerContexts = ctx.header();
        for (TestGeneratorParser.HeaderContext headerContext : headerContexts) {
            ST headersSt = stGroup.getInstanceOf("requestHeaders");
            List<TerminalNode> keyValuePairs = headerContext.STRING();
            String headerName = keyValuePairs.get(0).getText().substring(1, keyValuePairs.get(0).getText().length() - 1);
            headersSt.add("headerKey", headerName);
            String headerValue = keyValuePairs.get(1).getText().substring(1, keyValuePairs.get(1).getText().length() - 1);
            headersSt.add("headerValue", headerValue);
            combinedTemplate.add("content", headersSt);
        }

        return combinedTemplate;
    }

    @Override
    public ST visitBody(TestGeneratorParser.BodyContext ctx) {
        ST requestBody = stGroup.getInstanceOf("requestBody");
        String body = ctx.RAW_STRING().getText();
        body = body.substring(3, body.length() - 3);
        body = body.trim();
        return requestBody.add("body", body);
    }

    @Override
    public ST visitValidate(TestGeneratorParser.ValidateContext ctx) {
        ST validateSt = stGroup.getInstanceOf("combinedTemplate");
        try {
            List<TestGeneratorParser.ValidateElementContext> validateElementContexts = ctx.validateElement();
            for (TestGeneratorParser.ValidateElementContext validateElementContext : validateElementContexts) {
                ST visitElementSt = visit(validateElementContext);
                if (visitElementSt == null) {
                    continue;
                }
                validateSt.add("content", visitElementSt);
            }
        } catch (Exception e) {
            System.err.println("Error in validate: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return validateSt;
    }

    @Override
    public ST visitValidateElement(TestGeneratorParser.ValidateElementContext ctx) {
        try {
            if (ctx.statusCode() != null) {
                return visit(ctx.statusCode());
            } else if (ctx.responseBody() != null) {
                return visit(ctx.responseBody());
            } else if (ctx.responseHeaders() != null) {
                return visit(ctx.responseHeaders());
            }
        } catch (Exception e) {
            System.err.println("Error in validateElement: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return null;
    }

    @Override
    public ST visitStatusCode(TestGeneratorParser.StatusCodeContext ctx) {
        ST statusCodeSt = stGroup.getInstanceOf("assertStatusCode");
        statusCodeSt.add("code", ctx.INT().getText());
        return statusCodeSt;
    }

    @Override
    public ST visitResponseBody(TestGeneratorParser.ResponseBodyContext ctx) {
        ST combinedTemplate = stGroup.getInstanceOf("combinedTemplate");

        List<TestGeneratorParser.BodyContainsContext> bodyContainsContexts = ctx.bodyContains();
        for (TestGeneratorParser.BodyContainsContext bodyContainsContext : bodyContainsContexts) {
            ST bodyContainsSt = visit(bodyContainsContext);
            combinedTemplate.add("content", bodyContainsSt);
        }

        List<TestGeneratorParser.BodyExactContext> bodyExactContexts = ctx.bodyExact();
        for (TestGeneratorParser.BodyExactContext bodyExactContext : bodyExactContexts) {
            ST bodyExactSt = visit(bodyExactContext);
            combinedTemplate.add("content", bodyExactSt);
        }

        return combinedTemplate;
    }

    @Override
    public ST visitBodyContains(TestGeneratorParser.BodyContainsContext ctx) {
        ST combinedTemplate = stGroup.getInstanceOf("combinedTemplate");

        List<TerminalNode> propertyNames = ctx.STRING();
        for (TerminalNode propertyName : propertyNames) {
            ST bodyContainsSt = stGroup.getInstanceOf("assertBodyContains");
            String propertyNameText = propertyName.getText().substring(1, propertyName.getText().length() - 1);
            bodyContainsSt.add("prop", propertyNameText);
            combinedTemplate.add("content", bodyContainsSt);
        }

        return combinedTemplate;
    }

    @Override
    public ST visitBodyExact(TestGeneratorParser.BodyExactContext ctx) {
        ST combinedTemplate = stGroup.getInstanceOf("combinedTemplate");

        List<TestGeneratorParser.BodyExactPairContext> bodyExactPairContexts = ctx.bodyExactPair();
        for (TestGeneratorParser.BodyExactPairContext bodyExactPairContext : bodyExactPairContexts) {
            ST visit = visit(bodyExactPairContext);
            combinedTemplate.add("content", visit);
        }

        return combinedTemplate;
    }

    @Override
    public ST visitBodyExactPair(TestGeneratorParser.BodyExactPairContext ctx) {
        ST bodyExactSt = stGroup.getInstanceOf("assertBodyExact");
        List<TerminalNode> keyValuePairs = ctx.STRING();

        String key = keyValuePairs.get(0).getText();
        bodyExactSt.add("prop", key.substring(1, key.length() - 1));

        String value = keyValuePairs.get(1).getText();
        bodyExactSt.add("value", Helper.getValidJsonValue(value));

        return bodyExactSt;
    }

    @Override
    public ST visitResponseHeaders(TestGeneratorParser.ResponseHeadersContext ctx) {
        ST combinedTemplate = stGroup.getInstanceOf("combinedTemplate");

        List<TestGeneratorParser.HeaderContext> headerContexts = ctx.header();
        for (TestGeneratorParser.HeaderContext headerContext : headerContexts) {
            ST headerSt = stGroup.getInstanceOf("assertResponseHeader");
            List<TerminalNode> keyValuePairs = headerContext.STRING();
            String headerName = keyValuePairs.get(0).getText();
            headerSt.add("header", headerName.substring(1, headerName.length() - 1));
            String headerValue = keyValuePairs.get(1).getText();
            headerSt.add("value", headerValue.substring(1, headerValue.length() - 1));
            combinedTemplate.add("content", headerSt);
        }

        return combinedTemplate;
    }
}
