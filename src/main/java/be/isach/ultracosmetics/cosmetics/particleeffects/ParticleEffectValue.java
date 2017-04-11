package be.isach.ultracosmetics.cosmetics.particleeffects;

import lombok.val;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static be.isach.ultracosmetics.$.nil;

/**
 * Created on 17-4-11.
 */
public class ParticleEffectValue {

    public static final Properties value = new Properties();

    static {
        val file = new File("uc-particle.value");
        try {
            if (!file.exists()) file.createNewFile();
            value.load(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int getRepeat(String name) {
        String val = value.getProperty(name + ".repeat");
        if (nil(val)) {
            value.setProperty(name + ".repeat", (val = "1"));
            try {
                value.save(new FileOutputStream("uc-particle.value"), "");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return Integer.parseInt(val);
    }

}
