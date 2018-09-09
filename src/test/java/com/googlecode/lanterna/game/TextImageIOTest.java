package com.googlecode.lanterna.game;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import com.googlecode.lanterna.graphics.TextImage;

public class TextImageIOTest {

    @Test
    public void readTest() throws IOException {
        final TextImage textImage = TextImageIO
                        .read(new File("src/test/resources/com/googlecode/lanterna/game/image"));
        assertThat(textImage.getSize().getColumns(), is(2));
        assertThat(textImage.getSize().getRows(), is(4));
    }

}
