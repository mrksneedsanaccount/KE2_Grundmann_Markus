package src.propra.imageconverter;

import src.helperclasses.ProjectConstants;
import src.propra.conversionfacilitators.CommandLineInterpreter;
import src.propra.conversionfacilitators.Conversions;
import src.propra.conversionfacilitators.Decode;
import src.propra.conversionfacilitators.Encode;

public class ImageConverter {










    public static void main(String[] args) {

        CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.interpretInputs(args);
        String secondArgument = (String) args[1];

        if (secondArgument.startsWith(ProjectConstants.OUTPUT)) {
            Conversions conversions = new Conversions(commandLineInterpreter);
            conversions.initializeConversions();
            conversions.setPixels();
            conversions.executeConversion();
        } else if (secondArgument.startsWith(ProjectConstants.ENCODE)) {
            src.propra.conversionfacilitators.Encode encode = new Encode(commandLineInterpreter);
            encode.exectueConversion();

        } else if (secondArgument.startsWith(ProjectConstants.DECODE)) {
            src.propra.conversionfacilitators.Decode decode = new Decode(commandLineInterpreter);
            decode.executeConversion();
        }





    }




}
