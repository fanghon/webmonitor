package linkcrawler;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFX.Type;
import jfxtras.labs.dialogs.MonologFXBuilder;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.dialogs.MonologFXButtonBuilder;
public class LinkCrawlerFXDialogs {
	public static void showErrorDialog(String title, String message)
	{
		MonologFXButton mlb = MonologFXButtonBuilder.create()
                .defaultButton(true)
                .type(MonologFXButton.Type.OK)
                .build();

        MonologFX mono = MonologFXBuilder.create()
                .modal(true)
                .message(message)
                .titleText(title)
                .type(Type.ERROR)
                .button(mlb)
                .buttonAlignment(MonologFX.ButtonAlignment.CENTER)
                .build();

        mono.showDialog();
	}
	
	public static void showInfoDialog(String title, String message)
	{
		MonologFXButton mlb = MonologFXButtonBuilder.create()
                .defaultButton(true)
                .type(MonologFXButton.Type.OK)
                .build();

        MonologFX mono = MonologFXBuilder.create()
                .modal(true)
                .message(message)
                .titleText(title)
                .type(Type.INFO)
                .button(mlb)
                .buttonAlignment(MonologFX.ButtonAlignment.CENTER)
                .build();
        mono.showDialog();
	}
}
