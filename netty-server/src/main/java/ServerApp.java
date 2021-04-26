import input.InputParameter;

public class ServerApp {
    public static void main(String[] args) {
        InputParameter inputParameter = new InputParameter();
        try {
            inputParameter.setLocation("E:/temp").setPort(8989);
            new AppBootstrap(inputParameter).start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
