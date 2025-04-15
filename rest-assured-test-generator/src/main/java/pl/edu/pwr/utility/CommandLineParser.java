package pl.edu.pwr.utility;

public class CommandLineParser {
    private static final String inputFlag = "--in";
    private static final String outputFlag = "--out";

    public static ToolArguments parseCommandLine(String[] args) {
        String inputFile = null;
        String outputFile = null;

        if (args.length == 4) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(inputFlag) && i + 1 < args.length) {
                    inputFile = args[i + 1];
                } else if (args[i].equals(outputFlag) && i + 1 < args.length) {
                    outputFile = args[i + 1];
                }
            }
        } else if (args.length == 2) {
            if (args[0].equals(inputFlag)) {
                inputFile = args[1];
            }
        } else {
            System.err.println("""
                    Invalid number of arguments.
                    Expected: --in <input_file> --out <output_file> or --in <input_file>""");
        }

        return new ToolArguments(inputFile, outputFile);
    }
}
