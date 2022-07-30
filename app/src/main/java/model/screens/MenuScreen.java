package model.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import launcher.BaseGame;
import launcher.ClashRoyale;
import model.actors.BaseActor;
import model.actors.users.User;
import model.actors.users.UserLevel;
import model.utilities.AnimationUtilities;
import model.utilities.Audio;

/**
 * Menu screen implementation.
 */
public class MenuScreen extends BaseScreen {

  /**
   * for save file user.
   */
  public static class UserDatabase {
    public int currentXP;
    public UserLevel currentLevel;

    public User createUser() {
      User user = new User("P");
      user.setCurrentLevel(currentLevel);
      user.setCurrentXP(currentXP);
      return user;
    }
  }

  private Audio audio;
  private TextureAtlas atlas, atlasLabel;
  private Skin skin, skinLabel;
  private Table table;
  private TextButton buttonPlay, buttonExit, buttonLevel, buttonScore;
  private Label heading, level;

  private Json json;
  private FileHandle file = Gdx.files.local("bin/desc.json");
  private UserDatabase desc;
  private User user;
  
  public UserDatabase newUserDatabase(){
    UserDatabase desc = new UserDatabase();
    desc.currentXP = 10;                         
    desc.currentLevel = UserLevel.LVL2;
    return desc;
  }

  @Override
  public void initialize() {
    audio = Audio.playMenuMusic();
    final var background = new BaseActor(0, 0, super.getMainStage());
    background.setAnimation(AnimationUtilities.loadTexture("backgrounds/menuBackground.png"));
    background.setSize(ClashRoyale.WIDTH, ClashRoyale.HEIGHT);
    Gdx.input.setInputProcessor(super.getUiStage());

    atlas = new TextureAtlas("buttons/button.pack");
    skin = new Skin(Gdx.files.internal("buttons/menuSkin.json"), atlas);
    table = new Table(skin);
    table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    //creating configuration labelbutton


    //creating heading
    heading = new Label(ClashRoyale.TITLE, skin);
    atlasLabel = new TextureAtlas("buttons/scoreLabel.pack");
    skinLabel = new Skin(Gdx.files.internal("buttons/menuSkinLabel.json"), atlasLabel);

    //creating buttons
    buttonPlay = new TextButton("Play", skin);
    buttonPlay.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        //BaseGame.setActiveScreen(new GameScreen());
        System.out.print(user.toString());
        load();
        System.out.print(user.toString());
        buttonScore.setText("Score " +String.valueOf(user.getCurrentXP()));
      }
    });
    buttonPlay.pad(15);

    buttonExit = new TextButton("Exit", skin);
    buttonExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        //Gdx.app.exit();
        save(desc);
      }
    });  
    desc = newUserDatabase();
    
    user = desc.createUser();
    buttonExit.pad(15);
    buttonScore = new TextButton("Score " +String.valueOf(user.getCurrentXP()), skinLabel);
    buttonLevel = new TextButton(user.getCurrentLevel().toString(), skinLabel);
    table.add(heading);
    table.getCell(heading).spaceBottom(100);
    table.row();
    table.add(buttonPlay);
    table.row();
    table.add(buttonExit);
    super.getUiStage().addActor(table);
    super.getUiStage().addActor(buttonLevel);
    buttonLevel.setPosition(Gdx.graphics.getWidth()-buttonLevel.getWidth(), Gdx.graphics.getHeight()-buttonLevel.getHeight());
    super.getUiStage().addActor(buttonScore);
    buttonScore.setPosition(0, Gdx.graphics.getHeight()-buttonLevel.getHeight());

  }

  public void save(UserDatabase desc) {
    Json json = new Json();
    json.setOutputType(OutputType.json);
    file.writeString(json.prettyPrint(desc), false);
  }

  public void load() {
    Json json = new Json();
    desc = json.fromJson(UserDatabase.class, file);
    user = desc.createUser();

  }

  @Override
  public void update(final float dt) {
    if (Gdx.input.isKeyJustPressed(Keys.S)) {
      audio.stop();
      BaseGame.setActiveScreen(new GameScreen());
    }

  }
  @Override
  public void dispose() {
    super.dispose();
    this.atlas.dispose();
    this.skin.dispose();
  }

}
