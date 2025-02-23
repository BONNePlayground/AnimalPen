package lv.id.bonne.animalpen.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.config.adapters.ResourceLocationTypeAdapter;
import lv.id.bonne.animalpen.config.util.CommentGeneration;
import net.minecraft.resources.ResourceLocation;


/**
 * The configuration file that allows modifying some of settings.
 */
public class ConfigurationManager
{
    /**
     * Default constructor.
     */
    public ConfigurationManager()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        // Register type adapter for ResourceLocation
        builder.registerTypeAdapter(ResourceLocation.class, new ResourceLocationTypeAdapter());

        this.gson = builder.create();
    }


    /**
     * This method generates config if it is missing.
     */
    public void generateConfig()
    {
        this.reset();

        try
        {
            this.writeConfig(true);
        }
        catch (IOException e)
        {
            AnimalPen.LOGGER.error("Error Generating config file: ", e);
        }
    }


    /**
     * This returns the location of the config file.
     *
     * @return The config file location
     */
    private File getConfigFile()
    {
        return new File("config/animal_pen_config.json");
    }


    /**
     * This method reads the config file from file.
     */
    public void readConfig()
    {
        try (FileReader reader = new FileReader(this.getConfigFile()))
        {
            this.configuration = this.gson.fromJson(reader, Configuration.class);

            if (this.isInvalid())
            {
                this.configuration.setDefaults();
                this.writeConfig(false);
            }
        }
        catch (JsonSyntaxException var2)
        {
            this.reset();

            try
            {
                this.writeConfig(false);
            }
            catch (IOException ignore)
            {
            }

            AnimalPen.LOGGER.error("Failed to read config. Generated default one.");
        }
        catch (IOException var2)
        {
            this.generateConfig();
            AnimalPen.LOGGER.error("Failed to open config. Generated default one.");
        }
    }


    /**
     * Reload config configuration.
     */
    public void reloadConfig()
    {
        try (FileReader reader = new FileReader(this.getConfigFile()))
        {
            this.configuration = this.gson.fromJson(reader, Configuration.class);

            if (this.isInvalid())
            {
                AnimalPen.LOGGER.error("Failed to validate config.");
            }
        }
        catch (IOException var2)
        {
            AnimalPen.LOGGER.error("Failed to read config. " + var2.getMessage());
        }
    }


    /**
     * This method resets configs to default values.
     */
    protected void reset()
    {
        this.configuration = new Configuration();
        this.configuration.init();
    }


    /**
     * This method returns if configs were invalid.
     *
     * @return {@code true} if configs were invalid.
     */
    private boolean isInvalid()
    {
        return this.configuration == null || this.configuration.isInvalid();
    }


    /**
     * This method writes the config file.
     *
     * @throws IOException Exception if writing failed.
     */
    public void writeConfig(boolean overwrite) throws IOException
    {
        File dir = new File("config/");

        if (dir.exists() || dir.mkdirs())
        {
            if (this.getConfigFile().exists() && !overwrite)
            {
                // Create backup file.
                int backupNumber = 1;
                File backupFile;

                do
                {
                    backupFile = new File(dir, this.getConfigFile().getName() + ".bak" + backupNumber);
                    backupNumber++;
                }
                while (backupFile.exists());

                Files.copy(this.getConfigFile().toPath(), backupFile.toPath());
            }

            if (this.getConfigFile().exists() || this.getConfigFile().createNewFile())
            {
                try
                {
                    Path path = Paths.get(this.getConfigFile().toURI());
                    Files.write(path, CommentGeneration.writeWithComments(this.gson, this.configuration).getBytes());
                }
                catch (IllegalAccessException e)
                {
                    throw new IOException(e);
                }
            }
        }
    }


    public Configuration getConfiguration()
    {
        return this.configuration;
    }


    private final Gson gson;

    private Configuration configuration;
}