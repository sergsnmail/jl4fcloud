package input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InputParameter implements ServerParameter, HandlerParameter{

    private int DEFAULT_PORT = 8989;
    private Path location;
    private int port;

    public InputParameter setLocation(String location) throws IOException {
        if (location == null){
            throw new NullPointerException("Location parameter not set");
        }
        Path locPath = Paths.get(location);
        if (!Files.exists(locPath)){
            Files.createDirectory(locPath);
        }
        this.location = locPath;
        return this;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port == 0 ?DEFAULT_PORT:port;
    }

    @Override
    public Path getLocation() {
        return location;
    }

}
