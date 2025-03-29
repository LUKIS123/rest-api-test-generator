package pl.edu.pwr.compiler;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import pl.edu.pwr.grammar.TestGeneratorBaseVisitor;
import pl.edu.pwr.grammar.TestGeneratorParser;

import java.net.URI;
import java.net.URISyntaxException;
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
        return new ST("Terminal node:<n>").add("n", node.getText());
    }

    @Override
    public ST visitTest(TestGeneratorParser.TestContext ctx) {
        String testMethodName = ctx.NAME().getText();
        ST testMethodSt = stGroup.getInstanceOf("initTestMethod");
        testMethodSt.add("name", testMethodName);

        ST requestSt = visit(ctx.request());
        ST assertSt = visit(ctx.validate());

        testMethodSt.add("request", requestSt);
        testMethodSt.add("assert", assertSt);

        return testMethodSt;
    }

    @Override
    public ST visitRequest(TestGeneratorParser.RequestContext ctx) {
        ST requestSt = stGroup.getInstanceOf("makeRequest");

        ST urlSt = visit(ctx.url());
        ST methodSt = visit(ctx.method());

        requestSt.add("request", urlSt).add("method", methodSt);
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

    //    @Override
//    public ST visitStatusCode(TestGeneratorParser.StatusCodeContext ctx) {
//        ST st = stGroup.getInstanceOf("int");
//        st.add("i", ctx.INT().getText());
//        return st;
//    }

}