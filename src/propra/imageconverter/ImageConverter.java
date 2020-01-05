package src.propra.imageconverter;

import src.propra.conversion_facilitators.CommandLineInterpreter;
import src.propra.conversion_facilitators.Conversions;
import src.propra.conversion_facilitators.Decode;
import src.propra.conversion_facilitators.Encode;
import src.propra.helpers.ProjectConstants;

public class ImageConverter {










    public static void main(String[] args) {

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





    }




}
