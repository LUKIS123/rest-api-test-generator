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
        ST requestSt = stGroup.getInstanceOf("makeRequest");
        try {
            ST urlSt = visit(ctx.url()); // TODO: w przyszłości można dodać opcję żeby globalnie ustawić adres bazowy
            requestSt.add("request", urlSt);

            ST headersSt = visit(ctx.headers());
            requestSt.add("headers", headersSt);

            ST methodSt = visit(ctx.method());
            requestSt.add("method", methodSt);
        } catch (Exception e) {
            System.err.println("Error in request: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        return requestSt;
    }

    @Override
    public ST visitMethod(TestGeneratorParser.MethodContext ctx) {
        Map<String, String> templateNameMap = Map.of(
                "GET", "getRequest",
                "POST", "postRequest",
                "PUT", "putRequest",
                "PATCH", "patchRequest",
                "DELETE", "deleteRequest");

        String templateName = templateNameMap.get(ctx.HTTP_METHOD().getText());
        if (templateName == null) {
            throw new IllegalArgumentException("Unknown method: " + ctx.HTTP_METHOD().getText());
        }

        ST method = stGroup.getInstanceOf(templateName);
        method.add("path", "");
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
                ST requestAddressWithPortSt = stGroup.getInstanceOf("requestFullAddress");
                requestAddressWithPortSt.add("uri", baseURI).add("port", port).add("path", basePath);
                return requestAddressWithPortSt;
            }

            ST requestAddressSt = stGroup.getInstanceOf("requestAddress");
            requestAddressSt.add("uri", baseURI).add("path", basePath);
            return requestAddressSt;

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + url, e);
        }
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
        ST validateSt = stGroup.getInstanceOf("validateRequest");
        try {
            ST statusCodeSt = visit(ctx.statusCode());
            validateSt.add("responseCodeAssertion", statusCodeSt);

            ST bodyAssertionsSt = visit(ctx.responseBody());
            validateSt.add("responseBodyAssertions", bodyAssertionsSt);

            ST headerAssertionsSt = visit(ctx.responseHeaders());
            validateSt.add("responseHeadersAssertions", headerAssertionsSt);
        } catch (Exception e) {
            System.err.println("Error in validate: " + e.getMessage());
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
            ST bodyContainsSt = stGroup.getInstanceOf("assertBodyContains");
            String propertyName = bodyContainsContext.STRING().getText().substring(1, bodyContainsContext.STRING().getText().length() - 1);
            bodyContainsSt.add("prop", propertyName);
            combinedTemplate.add("content", bodyContainsSt);
        }

        List<TestGeneratorParser.BodyExactContext> bodyExactContexts = ctx.bodyExact();
        for (TestGeneratorParser.BodyExactContext bodyExactContext : bodyExactContexts) {
            ST bodyExactSt = stGroup.getInstanceOf("assertBodyExact");
            List<TerminalNode> keyValuePairs = bodyExactContext.STRING();
            bodyExactSt.add("prop", keyValuePairs.get(0).getText().substring(1, keyValuePairs.get(0).getText().length() - 1));
            bodyExactSt.add("value", keyValuePairs.get(1).getText().substring(1, keyValuePairs.get(1).getText().length() - 1));
            combinedTemplate.add("content", bodyExactSt);
        }

        return combinedTemplate;
    }

    @Override
    public ST visitResponseHeaders(TestGeneratorParser.ResponseHeadersContext ctx) {
        ST combinedTemplate = stGroup.getInstanceOf("combinedTemplate");

        List<TestGeneratorParser.HeaderContext> headerContexts = ctx.header();
        for (TestGeneratorParser.HeaderContext headerContext : headerContexts) {
            ST headerSt = stGroup.getInstanceOf("assertResponseHeader");
            List<TerminalNode> keyValuePairs = headerContext.STRING();
            headerSt.add("header", keyValuePairs.get(0).getText().substring(1, keyValuePairs.get(0).getText().length() - 1));
            headerSt.add("value", keyValuePairs.get(1).getText().substring(1, keyValuePairs.get(1).getText().length() - 1));
            combinedTemplate.add("content", headerSt);
        }

        return combinedTemplate;
    }
}
