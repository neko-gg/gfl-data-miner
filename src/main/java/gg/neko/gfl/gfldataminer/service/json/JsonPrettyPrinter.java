package gg.neko.gfl.gfldataminer.service.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonPrettyPrinter {

    public PrettyPrinter getPrettyPrinter() {
        return new CustomDefaultPrettyPrinter();
    }

    private static class CustomDefaultPrettyPrinter extends DefaultPrettyPrinter {

        public CustomDefaultPrettyPrinter() {
            super();
            DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
            _arrayIndenter = indenter;
            _objectIndenter = indenter;
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new CustomDefaultPrettyPrinter();
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            g.writeRaw(": ");
        }

    }

}
