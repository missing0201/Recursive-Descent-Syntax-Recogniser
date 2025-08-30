public class Generate extends AbstractGenerate {

    public void reportError(Token token, String explanatoryMessage) throws CompilationException {
        String message="Syntax Error : " + explanatoryMessage;
        throw new CompilationException(message);
    }

}
