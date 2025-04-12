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
        String testClassName = ctx.NAME().getText();

        ST baseUrlSt = visit(ctx.url());

        List<TestGeneratorParser.TestContext> testContexts = ctx.test();
        for (TestGeneratorParser.TestContext testContext : testContexts) {
            ST testMethodSt = visit(testContext);
        }

        return super.visitClassDef(ctx);
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
            // TODO: w przyszłości można dodać opcję żeby globalnie ustawić adres bazowy
            ST urlSt = visit(ctx.url());
            requestSt.add("content", urlSt);

            ST headersSt = visit(ctx.headers());
            requestSt.add("content", headersSt);
// poprawic
            ST querySt = visit(ctx.queryParams());
            requestSt.add("content", querySt);

            ST methodSt = visit(ctx.method());
            requestSt.add("content", methodSt);
        } catch (Exception e) {
            System.err.println("Error in request: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return requestSt;
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

            Integer port = null;
            String baseURI = uri.getScheme() + "://" + uri.getHost();
            String basePath = null;

            if (uri.getPort() != -1) {
                port = uri.getPort();
            }

            if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                basePath = uri.getPath();
            }

            if (port != null) {
                ST requestAddressWithPortSt = stGroup.getInstanceOf("requestWithPortAndPath");
                requestAddressWithPortSt.add("uri", baseURI).add("port", port).add("path", basePath);
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
    public ST visitValidate(TestGeneratorParser.ValidateContext ctx) {
        ST validateSt = stGroup.getInstanceOf("combinedTemplate");
        try {
            List<TestGeneratorParser.ValidateElementContext> validateElementContexts = ctx.validateElement();
            for (TestGeneratorParser.ValidateElementContext validateElementContext : validateElementContexts) {
                ST visitElementSt = visit(validateElementContext);
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
        ST validateSt = stGroup.getInstanceOf("combinedTemplate");
        try {
            ST statusCodeSt = visit(ctx.statusCode());
            validateSt.add("content", statusCodeSt);

            ST bodyAssertionsSt = visit(ctx.responseBody());
            validateSt.add("content", bodyAssertionsSt);

            ST headerAssertionsSt = visit(ctx.responseHeaders());
            validateSt.add("content", headerAssertionsSt);
        } catch (Exception e) {
            System.err.println("Error in validateElement: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return validateSt;
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
