package meteordevelopment.meteorclient.systems.modules.Own;


import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;


public class ChatTranslator extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<ChatTranslator.Language> language = sgGeneral.add(new EnumSetting.Builder<ChatTranslator.Language>().name("mode").description("Language the chat will be translated to.").defaultValue(Language.English).build());


    public ChatTranslator() {
        super(Categories.Own, "ChatTranslator", "Translates the Minecraft chat for you.");
    }
    //Test


    private enum Language {
        English("en"), Deutsch("de"), Polish("pl");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
