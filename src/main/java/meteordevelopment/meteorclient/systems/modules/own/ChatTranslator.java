package meteordevelopment.meteorclient.systems.modules.own;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;



public class ChatTranslator extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Language> language = sgGeneral.add(new EnumSetting.Builder<Language>()
        .name("Language:")
        .description("Language the chat will be translated to.")
        .defaultValue(Language.English)
        .build());

    public ChatTranslator() {
        super(Categories.Own, "Chat Translator", "Translates the Minecraft chat for you.");
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        if (!event.isCancelled() && !event.isModified()) {
            // Translate the message based on the selected language
            Text translatedMessage = translate(event.getMessage(), language.get());
            // Set the translated message back to the event
            event.setMessage(translatedMessage);
        }
    }
    private Text translate(Text originalMessage, Language language) {
        // Initialize the Google Cloud Translate client
        Translate translate = TranslateOptions.newBuilder().setApiKey("YOUR_API_KEY").build().getService();


        // Get the language code corresponding to the selected language
        String languageCode = LANGUAGE_CODES.get(language);

        // Perform translation
        Translation translation = translate.translate(
            originalMessage.getString(),
            Translate.TranslateOption.targetLanguage(languageCode)
        );

        // Print the original and translated messages
        System.out.println("Original Message: " + originalMessage.getString());
        System.out.println("Translated Message: " + translation.getTranslatedText());

        // Return the translated message
        return Text.of(translation.getTranslatedText());
    }
    private static final Map<Language, String> LANGUAGE_CODES = new HashMap<>();
    static {
        LANGUAGE_CODES.put(Language.English, "en");
        LANGUAGE_CODES.put(Language.Deutsch, "de");
        LANGUAGE_CODES.put(Language.Polish, "pl");
        // Add more language codes as needed
    }
    private enum Language {
        English, Deutsch, Polish
    }
}
