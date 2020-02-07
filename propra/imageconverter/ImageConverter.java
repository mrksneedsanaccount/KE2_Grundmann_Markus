package propra.imageconverter;

import propra.conversion_facilitators.CommandLineInterpreter;
import propra.conversion_facilitators.Conversions;
import propra.conversion_facilitators.Decode;
import propra.conversion_facilitators.Encode;
import propra.helpers.ProjectConstants;

public class ImageConverter {


    public static void main(String[] args) {

        try {
            CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
            commandLineInterpreter.interpretInputs(args);
            String secondArgument = args[1];

            if (secondArgument.startsWith(ProjectConstants.OUTPUT)) {
                Conversions conversions = new Conversions(commandLineInterpreter);
                conversions.initializeConversions();
                conversions.setPixels();
                conversions.executeConversion();
            } else if (secondArgument.startsWith(ProjectConstants.ENCODE)) {
                Encode encode = new Encode(commandLineInterpreter);
                encode.exectueConversion();

            } else if (secondArgument.startsWith(ProjectConstants.DECODE)) {
                Decode decode = new Decode(commandLineInterpreter);
                decode.executeConversion();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("A critical exception has occured. The program has been terminated.");
//            // System.exit(123);
        }


    }




}
