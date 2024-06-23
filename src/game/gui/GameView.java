package game.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;

import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

import game.engine.*;
import game.engine.titans.*;
import game.engine.weapons.*;
import game.engine.lanes.*;
import game.engine.exceptions.*;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class GameView extends Application {

	Battle game;
	private Stage primaryStage;
	private final BorderPane root = new BorderPane();

	// Game settings for Easy mode
	private final int EASY_NUM_LANES = 3;
	private final int EASY_RESOURCES_PER_LANE = 250;

	// Game settings for Hard mode
	private final int HARD_NUM_LANES = 5;
	private final int HARD_RESOURCES_PER_LANE = 125;

	// score,turn,resources,Titans Distance
	private int score = 0;
	private int turn = 1;
	private int resources = 0;
	private final int titanSpawnDistance = 100; // Adjust this value based on your screen size

	// Labels
	private Label scoreLabel;
	private Label turnLabel;
	private Label resourceLabel;
	private Label phaseLabel;

	// WeaponShop
	private HBox weaponButtonsContainer;
	// Lanes
	private VBox laneContainer;

	// Styling
	private MediaPlayer mediaPlayer;
	private MediaPlayer videoPlayer;
	private int currentImageIndex = 0;
	private final String[] imageUrls = { "Pic1.png", "wall maria.png", "wall rose.png", "wall sina.png",
			"intense phase.png", "tower defense game.png"
			// Add more image URLs for your slideshow
	};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Method to automate player actions by choosing the best action to do
	private void automatePlayerActions() {
		// Evaluate the game state
		int resources = game.getResourcesGathered();
		List<Lane> lanes = game.getOriginalLanes();
		HashMap<Integer, WeaponRegistry> weaponShopData = game.getWeaponFactory().getWeaponShop();

		// Determine the best action
		for (Lane lane : lanes) {
			if (lane.getLaneWall().getCurrentHealth() <= 0) {
				continue; // Skip lanes with destroyed walls
			}

			// Check for titans in the lane
			PriorityQueue<Titan> titans = lane.getTitans();
			if (!titans.isEmpty()) {
				// Prioritize lanes with titans and purchase the most powerful affordable weapon
				WeaponRegistry bestWeapon = null;
				for (WeaponRegistry weaponRegistry : weaponShopData.values()) {
					if (resources >= weaponRegistry.getPrice()
							&& (bestWeapon == null || weaponRegistry.getDamage() > bestWeapon.getDamage())) {
						bestWeapon = weaponRegistry;
					}
				}

				if (bestWeapon != null) {
					try {
						// Purchase the weapon and place it in the lane
						game.purchaseWeapon(bestWeapon.getCode(), lane);
						resources -= bestWeapon.getPrice();
						updateGameViews(); // Update game views after purchase
						return; // Action taken, exit the method
					} catch (InvalidLaneException | InsufficientResourcesException e) {
						e.printStackTrace(); // Handle exceptions appropriately
					}
				}
			}
		}

		// If no titans are present, purchase the most powerful affordable weapon and
		// place it in a random lane
		WeaponRegistry bestWeapon = null;
		for (WeaponRegistry weaponRegistry : weaponShopData.values()) {
			if (resources >= weaponRegistry.getPrice()
					&& (bestWeapon == null || weaponRegistry.getDamage() > bestWeapon.getDamage())) {
				bestWeapon = weaponRegistry;
			}
		}

		if (bestWeapon != null) {
			for (Lane lane : lanes) {
				if (lane.getLaneWall().getCurrentHealth() > 0) {
					try {
						// Purchase the weapon and place it in the lane
						game.purchaseWeapon(bestWeapon.getCode(), lane);
						resources -= bestWeapon.getPrice();
						updateGameViews(); // Update game views after purchase
						return; // Action taken, exit the method
					} catch (InvalidLaneException | InsufficientResourcesException e) {
						e.printStackTrace(); // Handle exceptions appropriately
					}
				}
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		primaryStage.setTitle("ATTACK ON TITANS:UTOPIA");
		Image GAME_PHOTO = new Image("ATTACK_ON_TITANS.jpg");
		GAME_PHOTO.getPixelReader();
		primaryStage.getIcons().add(GAME_PHOTO);

		setupInitialScreen();
		playBackgroundMusic("src/Attack On Titan.mp3");

		primaryStage.show();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setupInitialScreen() {
		VBox menuBox = new VBox(20);
		menuBox.setAlignment(Pos.CENTER);

		// Container for the background image
		Region backgroundRegion = new Region();
		backgroundRegion.setPrefSize(1000, 625);

		Label titleLabel = new Label("Press Start Game To Play");
		titleLabel.setStyle("-fx-font-size: 30px; " + "-fx-font-weight: bold; " + "-fx-text-fill: #000000;");
		titleLabel.setPadding(new Insets(5));
		DropShadow borderEffect = new DropShadow();
		borderEffect.setColor(Color.web("#0000FF"));
		titleLabel.setEffect(borderEffect);

		Button startButton = createStyledButton("Start Game", 180, 40);
		startButton.setOpacity(1);
		startButton.setOnAction(event -> playIntroVideo());

		Button titansButton = createStyledButton("Titans", 180, 40);
		titansButton.setOnAction(event -> showTitans());

		Button weaponsButton = createStyledButton("Weapons", 180, 40);
		weaponsButton.setOnAction(event -> showWeapons());

		// Create a Timeline to periodically change the background image
		Timeline backgroundTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
			currentImageIndex = (currentImageIndex + 1) % imageUrls.length;
			String imageUrl = imageUrls[currentImageIndex];
			setBackgroundImage(backgroundRegion, imageUrl);
		}));
		backgroundTimeline.setCycleCount(Timeline.INDEFINITE);
		backgroundTimeline.play();

		menuBox.getChildren().addAll(titleLabel, startButton, titansButton, weaponsButton);

		StackPane root = new StackPane();
		root.getChildren().addAll(backgroundRegion, menuBox);

		primaryStage.setScene(new Scene(root, 1000, 625, Color.BLACK));
	}

	private void showTitans() {
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(20);
		gridPane.setVgap(20);

		// First Titan
		ImageView imageView1 = new ImageView("colossal titan.png");
		imageView1.setFitWidth(200);
		imageView1.setFitHeight(200);
		Text text1 = new Text("Colossal Titan");
		text1.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect1 = new DropShadow();
		borderEffect1.setColor(Color.web("#0000FF"));
		text1.setEffect(borderEffect1);

		// Second Titan
		ImageView imageView2 = new ImageView("pure titan.png");
		imageView2.setFitWidth(200);
		imageView2.setFitHeight(200);
		Text text2 = new Text("Pure Titan");
		text2.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect2 = new DropShadow();
		borderEffect2.setColor(Color.web("#0000FF"));
		text2.setEffect(borderEffect2);

		ImageView imageView3 = new ImageView("armored titan.png");
		imageView3.setFitWidth(200);
		imageView3.setFitHeight(200);
		Text text3 = new Text("Armored Titan");
		text3.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect3 = new DropShadow();
		borderEffect3.setColor(Color.web("#0000FF"));
		text3.setEffect(borderEffect3);

		ImageView imageView4 = new ImageView("abnormal titan.png");
		imageView4.setFitWidth(200);
		imageView4.setFitHeight(200);
		Text text4 = new Text("Abnormal Titan");
		text4.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect4 = new DropShadow();
		borderEffect4.setColor(Color.web("#0000FF"));
		text4.setEffect(borderEffect4);

		gridPane.add(imageView1, 0, 0);
		gridPane.add(text1, 0, 1);
		gridPane.add(imageView2, 3, 0);
		gridPane.add(text2, 3, 1);
		gridPane.add(imageView3, 0, 2);
		gridPane.add(text3, 0, 3);
		gridPane.add(imageView4, 3, 2);
		gridPane.add(text4, 3, 3);

		// Return Button
		Button returnButton = createStyledButton("Return To Menu", 180, 40);
		returnButton.setOnAction(event -> setupInitialScreen());
		HBox returnBox = new HBox(returnButton);
		returnBox.setAlignment(Pos.BOTTOM_CENTER);

		VBox mainBox = new VBox(20, gridPane, returnBox);
		mainBox.setAlignment(Pos.CENTER);
		mainBox.setPadding(new Insets(20));
		mainBox.setStyle("-fx-background-color: black;");

		Scene scene = new Scene(mainBox, 1000, 625);
		primaryStage.setScene(scene);
	}

	private void showWeapons() {
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(20);
		gridPane.setVgap(20);

		ImageView imageView1 = new ImageView("wall trap.png");
		imageView1.setFitWidth(200);
		imageView1.setFitHeight(200);
		Text text1 = new Text("Wall Trap");
		text1.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect1 = new DropShadow();
		borderEffect1.setColor(Color.web("#0000FF"));
		text1.setEffect(borderEffect1);

		ImageView imageView2 = new ImageView("Piercing Canon.png");
		imageView2.setFitWidth(200);
		imageView2.setFitHeight(200);
		Text text2 = new Text("Piercing Cannon");
		text2.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect2 = new DropShadow();
		borderEffect2.setColor(Color.web("#0000FF"));
		text2.setEffect(borderEffect2);

		ImageView imageView3 = new ImageView("Volley Spread Cannon.png");
		imageView3.setFitWidth(200);
		imageView3.setFitHeight(200);
		Text text3 = new Text("Volley Spread Cannon");
		text3.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect3 = new DropShadow();
		borderEffect3.setColor(Color.web("#0000FF"));
		text3.setEffect(borderEffect3);

		ImageView imageView4 = new ImageView("Sniper Canon.png");
		imageView4.setFitWidth(200);
		imageView4.setFitHeight(200);
		Text text4 = new Text("Sniper Cannon");
		text4.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
		DropShadow borderEffect4 = new DropShadow();
		borderEffect4.setColor(Color.web("#0000FF"));
		text4.setEffect(borderEffect4);

		gridPane.add(imageView1, 0, 0);
		gridPane.add(text1, 0, 1);
		gridPane.add(imageView2, 3, 0);
		gridPane.add(text2, 3, 1);
		gridPane.add(imageView3, 0, 2);
		gridPane.add(text3, 0, 3);
		gridPane.add(imageView4, 3, 2);
		gridPane.add(text4, 3, 3);

		// Return Button
		Button returnButton = createStyledButton("Return To Menu", 180, 40);
		returnButton.setOnAction(event -> setupInitialScreen());
		HBox returnBox = new HBox(returnButton);
		returnBox.setAlignment(Pos.BOTTOM_CENTER);

		VBox mainBox = new VBox(20, gridPane, returnBox);
		mainBox.setAlignment(Pos.CENTER);
		mainBox.setPadding(new Insets(20));
		mainBox.setStyle("-fx-background-color: black;");

		Scene scene = new Scene(mainBox, 1000, 625);
		primaryStage.setScene(scene);

	}

	private void showDifficultySelection() {
		VBox difficultyBox = new VBox(20);
		difficultyBox.setAlignment(Pos.CENTER);

		// Container for the background image
		Region backgroundRegion = new Region();
		backgroundRegion.setPrefSize(1000, 600);

		// Create a Timeline to periodically change the background image
		Timeline backgroundTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			currentImageIndex = (currentImageIndex + 1) % imageUrls.length;
			String imageUrl = imageUrls[currentImageIndex];
			setBackgroundImage(backgroundRegion, imageUrl);
		}));
		backgroundTimeline.setCycleCount(Timeline.INDEFINITE);
		backgroundTimeline.play();

		Label chooseLabel = new Label("Choose Difficulty:");
		chooseLabel.setStyle("-fx-font-size: 50px; " + "-fx-font-weight: bold; " + "-fx-text-fill: #000000;");
		chooseLabel.setPadding(new Insets(5)); // Adjust the padding as needed

		DropShadow borderEffect = new DropShadow();
		borderEffect.setColor(Color.web("#0000FF")); // Blue color for the drop shadow
		chooseLabel.setEffect(borderEffect); // Apply the drop shadow effect to the label

		Button easyButton = createStyledButton("Easy", 180, 40);
		easyButton.setOnAction(event -> showGameInstructions(EASY_NUM_LANES, EASY_RESOURCES_PER_LANE));

		Button hardButton = createStyledButton("Hard", 180, 40);
		hardButton.setOnAction(event -> showGameInstructions(HARD_NUM_LANES, HARD_RESOURCES_PER_LANE));

		difficultyBox.getChildren().addAll(chooseLabel, easyButton, hardButton);

		StackPane root = new StackPane();
		root.getChildren().addAll(backgroundRegion, difficultyBox);

		primaryStage.setScene(new Scene(root, 1000, 600, Color.BLACK));
	}

	private void showGameInstructions(int numLanes, int resourcesPerLane) {
		VBox instructionsBox = new VBox(20);
		instructionsBox.setAlignment(Pos.CENTER);

		Label instructionsLabel = new Label();
		if (numLanes == EASY_NUM_LANES && resourcesPerLane == EASY_RESOURCES_PER_LANE) {
			// Instructions for Easy mode
			instructionsLabel.setText(
					"Welcome to Easy mode!\n\n" + "Instructions:\n" + "- Defend the lanes with the given resources.\n"
							+ "- Purchase weapons from the shop to defend against titans.\n"
							+ "- Pass turns to progress the game and face more challenges.\n\n"
							+ "Press Proceed to start the game.");

		} else if (numLanes == HARD_NUM_LANES && resourcesPerLane == HARD_RESOURCES_PER_LANE) {
			// Instructions for Hard mode
			instructionsLabel.setText("Welcome to Hard mode!\n\n" + "Instructions:\n"
					+ "- Defend multiple lanes with limited resources.\n"
					+ "- Carefully choose your weapons to maximize defense.\n"
					+ "- Survive against increasing titan attacks.\n\n" + "Press Proceed to start the game.");

		}
		instructionsLabel.setStyle("-fx-font-size: 20px; " + "-fx-font-weight: bold; " + "-fx-text-fill: #FF0000;");

		// Set background color to black
		instructionsBox.setStyle("-fx-background-color: black;");

		// Proceed button to start the game
		Button proceedButton = createStyledButton("Proceed", 180, 40);
		proceedButton.setOnAction(event -> {
			initializeGame(numLanes, resourcesPerLane);

		});

		instructionsBox.getChildren().addAll(instructionsLabel, proceedButton);
		primaryStage.setScene(new Scene(instructionsBox, 1000, 600, Color.BLACK));
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Game Initilization:
	private void initializeGame(int numLanes, int resourcesPerLane) {
		resources = 0;
		score = 0;
		turn = 1;
		setResources(numLanes * resourcesPerLane); // Total resources for all lanes
		try {
			game = new Battle(turn, score, titanSpawnDistance, numLanes, resourcesPerLane);

			setupGame(numLanes);
			updateGameViews();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Game Setup setting lane and weapon_shop
	private void setupGame(int numlanes) {
		laneContainer = new VBox(10);
		laneContainer.setAlignment(Pos.CENTER);

		weaponButtonsContainer = new HBox(10);
		weaponButtonsContainer.setAlignment(Pos.CENTER);
		GridPane laneGrid = new GridPane();
		laneGrid.setAlignment(Pos.CENTER);
		laneGrid.setHgap(10); // Horizontal gap between grid elements
		laneGrid.setVgap(10); // Vertical gap between grid elements

		root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

		initializeInfoBox(root);
		initializeControlBox(root);
		populateWeaponShop(weaponButtonsContainer);
		primaryStage.setScene(new Scene(root, 1000, 600));
		primaryStage.show();

	}

	// Top Info
	private void initializeInfoBox(BorderPane root) {
		HBox infoBox = new HBox(20);
		infoBox.setAlignment(Pos.CENTER);
		scoreLabel = new Label();
		turnLabel = new Label();
		resourceLabel = new Label();
		phaseLabel = new Label();

		infoBox.getChildren().addAll(scoreLabel, turnLabel, resourceLabel, phaseLabel);
		root.setTop(infoBox);
	}

	private void initializeControlBox(BorderPane root) {
		// Bottom panel for game controls
		HBox controlBox = new HBox(30);
		controlBox.setAlignment(Pos.CENTER);

		// Create a titled border for the weapon shop
		TitledPane weaponShopContainer = new TitledPane("The Weapon Factory", null);
		weaponShopContainer.setAlignment(Pos.CENTER);
		weaponShopContainer.setCollapsible(true);
		weaponShopContainer.setBorder(new Border(
				new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.FULL)));

		// Create an HBox to hold the weapon buttons
		weaponButtonsContainer = new HBox(10); // Changed to use the instance variable
		weaponButtonsContainer.setAlignment(Pos.CENTER); // Center-align the buttons
		weaponButtonsContainer.setStyle("-fx-background-color: black;"); // Set background to black
		weaponShopContainer.setContent(weaponButtonsContainer); // Set the HBox as content of the TitledPane

		Button passTurnButton = createStyledButton("Pass Turn", 180, 40);
		passTurnButton.setOnAction(event -> {
			passTurn(); // Progress the game state
			turn++; // Increment the turn counter
		});

		controlBox.getChildren().add(passTurnButton);
		VBox bottomBox = new VBox(20, controlBox, weaponShopContainer);
		bottomBox.setAlignment(Pos.CENTER);
		root.setBottom(bottomBox);
		root.setCenter(new HBox(20, laneContainer));
		// Populate the weapon shop with buttons
		populateWeaponShop(weaponButtonsContainer);
	}

	private void passTurn() {
		game.passTurn(); // Assuming this method updates the turn in your Battle class
		updateGameViews(); // Refresh UI to reflect new game state
	}

	private void updateGameViews() {

		scoreLabel.setText("Score: " + game.getScore()); // Update scoreLabel with the new score
		// Label:///////////////////////////////////////////////////////////////////////////////////
		scoreLabel.setStyle("-fx-font-size: 15px; " + "-fx-font-weight: bold; " + "-fx-text-fill: black; ");
		scoreLabel.setPadding(new Insets(5));
		DropShadow borderEffect1 = new DropShadow();
		borderEffect1.setColor(Color.web("#FF0000"));
		scoreLabel.setEffect(borderEffect1);
		////////////////////////////////////////////////////////////////////////////////

		turnLabel.setText("Turn: " + game.getNumberOfTurns());
		// Label:///////////////////////////////////////////////////////////////////////////////////
		turnLabel.setStyle("-fx-font-size: 15px; " + "-fx-font-weight: bold; " + "-fx-text-fill: black; ");
		turnLabel.setPadding(new Insets(5));
		DropShadow borderEffect2 = new DropShadow();
		borderEffect2.setColor(Color.web("#FF0000"));
		turnLabel.setEffect(borderEffect2);
		////////////////////////////////////////////////////////////////////////////////

		resourceLabel.setText("Resources: " + game.getResourcesGathered());
		// Label:///////////////////////////////////////////////////////////////////////////////////
		resourceLabel.setStyle("-fx-font-size: 15px; " + "-fx-font-weight: bold; " + "-fx-text-fill: black; ");
		resourceLabel.setPadding(new Insets(5));
		DropShadow borderEffect3 = new DropShadow();
		borderEffect3.setColor(Color.web("#FF0000"));
		resourceLabel.setEffect(borderEffect3);
		////////////////////////////////////////////////////////////////////////////////

		phaseLabel.setText("Phase: " + game.getBattlePhase().toString());
		// Label:///////////////////////////////////////////////////////////////////////////////////
		phaseLabel.setStyle("-fx-font-size: 15px; " + "-fx-font-weight: bold; " + "-fx-text-fill: black; ");
		phaseLabel.setPadding(new Insets(5));
		DropShadow borderEffect4 = new DropShadow();
		borderEffect4.setColor(Color.web("#FF0000"));
		phaseLabel.setEffect(borderEffect4);
		////////////////////////////////////////////////////////////////////////////////

		if (game.isGameOver()) {
			gameOver();
		} else {
			updateLanes();
			populateWeaponShop(weaponButtonsContainer);

			// Change background image based on game phase
			switch (game.getBattlePhase()) {
			case EARLY:
				setBackgroundImageFull(root, "early phase.png");

				break;
			case GRUMBLING:
				setBackgroundImageFull(root, "grumbling phase.png");
				break;
			case INTENSE:
				setBackgroundImageFull(root, "intense phase.png");
				break;
			// Add more cases for other phases as needed
			default:
				setBackgroundImageFull(root, "wall2.png");
				break;
			}
		}
	}

	private void updateLanes() {
		laneContainer.getChildren().clear();
		GridPane laneGrid = new GridPane();
		laneGrid.setHgap(10);
		laneGrid.setVgap(10);
		laneContainer.getChildren().add(laneGrid);

		List<Lane> lanes = new ArrayList<>(game.getLanes());
		for (int i = 0; i < lanes.size(); i++) {
			Lane lane = lanes.get(i);
			GridPane laneBox = createLaneBox(lane, i); // Pass the lane and index to create the lane box
			laneGrid.add(laneBox, i % 1, i / 1); // Place laneBox in a grid, 1 column wide
		}
	}

	private GridPane createLaneBox(Lane lane, int index) {
	    GridPane laneBox = new GridPane();
	    laneBox.setHgap(10);
	    laneBox.setVgap(10);
	    laneBox.setPadding(new Insets(10));

	    String laneStatus = (lane.getLaneWall().getCurrentHealth() <= 0) ? " (Lost)" : "";

	    // Lane details
	    Label laneLabel = new Label("Lane " + (index + 1) + laneStatus);
	    laneLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: black;");
	    laneLabel.setPadding(new Insets(5));
	    DropShadow borderEffect1 = new DropShadow();
	    borderEffect1.setColor(Color.web("#0000FF"));
	    laneLabel.setEffect(borderEffect1);

	    Label laneDetailsLabel = new Label(
	            "Wall Health: " + lane.getLaneWall().getCurrentHealth() + "\nDanger Level: " + lane.getDangerLevel());
	    laneDetailsLabel.setStyle("-fx-font-size: 5px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
	    laneDetailsLabel.setPadding(new Insets(5));
	    DropShadow borderEffect2 = new DropShadow();
	    borderEffect2.setColor(Color.web("#0000FF"));
	    laneDetailsLabel.setEffect(borderEffect2);

	    // Weapons Box
	    HBox weaponsBox = new HBox(5); // Use HBox instead of VBox for horizontal alignment
	    weaponsBox.setAlignment(Pos.CENTER_LEFT);
	    for (Weapon weapon : lane.getWeapons()) {
	        String weaponPurchased = WeaponBought(weapon);
	        ImageView weaponImageView = new ImageView(new Image(weaponPurchased));
	        weaponImageView.setFitWidth(50);
	        weaponImageView.setFitHeight(50);

	        Label weaponDetails = new Label(
	                "Name: " + weapon.getClass().getSimpleName() + "\nDamage: " + weapon.getDamage());
	        weaponDetails.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
	        weaponDetails.setFont(new Font(10));
	        weaponDetails.setStyle("-fx-font-size: 5px; -fx-font-weight: bold; -fx-text-fill: black;");
	        weaponDetails.setPadding(new Insets(5));
	        DropShadow borderEffect4 = new DropShadow();
	        borderEffect4.setColor(Color.web("#0000FF"));
	        weaponDetails.setEffect(borderEffect4);

	        VBox weaponBox = new VBox(weaponImageView, weaponDetails);
	        weaponsBox.getChildren().add(weaponBox);
	    }

	    // Titans Box
	    HBox titansBox = new HBox(10); // Changed to HBox for horizontal alignment
	    titansBox.setAlignment(Pos.CENTER_LEFT);

	    List<TranslateTransition> transitions = new ArrayList<>();

	    for (Titan t : lane.getTitans()) {
	        if (t.getCurrentHealth() <= 0) {
	            continue;
	        }

	        GridPane titanGrid = new GridPane();
	        titanGrid.setHgap(5);
	        titanGrid.setVgap(5);

	        ImageView imageView = new ImageView(TitanImage(t));
	        imageView.setFitWidth(50);
	        imageView.setFitHeight(50);

	        // Add tooltip to Titan image
	        Tooltip tooltip = new Tooltip("Health: " + t.getCurrentHealth() + "\nHeight: " + t.getHeightInMeters() + "m"
	                + "\nPosition: " + t.getDistance() + "m" + "\nSpeed: " + t.getSpeed() + "m/turn");
	        styleTooltip(tooltip); // Apply custom CSS styling
	        Tooltip.install(imageView, tooltip);

	        Label titanDetailsLabel = new Label("Health: " + t.getCurrentHealth() + "\nHeight: " + t.getHeightInMeters()
	                + "m" + "\nPosition: " + t.getDistance() + "m" + "\nSpeed: " + t.getSpeed() + "m/turn");
	        titanDetailsLabel.setFont(new Font(10));
	        titanDetailsLabel.setStyle("-fx-font-size: 5px; -fx-font-weight: bold; -fx-text-fill: black;");
	        titanDetailsLabel.setPadding(new Insets(5));
	        DropShadow borderEffect4 = new DropShadow();
	        borderEffect4.setColor(Color.web("#0000FF"));
	        titanDetailsLabel.setEffect(borderEffect4);

	        titanGrid.add(imageView, 0, 0, 1, 2);
	        titanGrid.add(titanDetailsLabel, 0, 3);

	        // Create translation transition for each titan
	        TranslateTransition transition = new TranslateTransition(Duration.seconds(1), titanGrid);
	        transition.setFromX(500); // Change this value according to your need
	        transition.setToX(0);
	        transition.setCycleCount(1);

	        transitions.add(transition);

	        titansBox.getChildren().add(titanGrid);
	    }

	    // Use SequentialTransition to play each transition sequentially
	    SequentialTransition sequentialTransition = new SequentialTransition();
	    sequentialTransition.getChildren().addAll(transitions);
	    sequentialTransition.play();

	    if (lane.getLaneWall().getCurrentHealth() <= 0) {
	        laneBox.setStyle("-fx-background-color: #FFCCCC;"); // Light red background for lost lanes
	        laneBox.setPadding(new Insets(5)); // Add padding for better visual separation
	    }

	    laneBox.add(laneLabel, 0, 0);
	    laneBox.add(laneDetailsLabel, 0, 1);
	    laneBox.add(weaponsBox, 0, 2);
	    laneBox.add(titansBox, 1, 0, 1, 3); // Spanning over 3 rows

	    return laneBox;
	}


	private void styleTooltip(Tooltip tooltip) {
		tooltip.setStyle("-fx-background-color: black; -fx-text-fill: white;");
	}

	private void populateWeaponShop(HBox weaponButtonsContainer) {
		weaponButtonsContainer.getChildren().clear(); // Clear existing weapon buttons
		HashMap<Integer, WeaponRegistry> weaponShopData = game.getWeaponFactory().getWeaponShop();
		if (weaponShopData.isEmpty()) {
			Label noWeaponsLabel = new Label("No weapons available yet!");
			noWeaponsLabel.setStyle("-fx-text-fill: white;"); // Set text color to white for better contrast
			weaponButtonsContainer.getChildren().add(noWeaponsLabel);
			return;
		}
		for (WeaponRegistry weaponRegistry : weaponShopData.values()) {
			VBox weaponBox = new VBox(10);
			weaponBox.setAlignment(Pos.CENTER);
			weaponBox.setPadding(new Insets(10));

			Label weaponLabel = new Label("Name: " + weaponRegistry.getName() + "\nPrice: " + weaponRegistry.getPrice()
					+ "\nDamage: " + weaponRegistry.getDamage());
			weaponLabel.setFont(new Font(10));
			weaponLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white;");
			weaponLabel.setPadding(new Insets(5));
			DropShadow borderEffect4 = new DropShadow();
			borderEffect4.setColor(Color.web("#0000FF"));
			weaponLabel.setEffect(borderEffect4);

			Button weaponButton = createStyledButton("buy", 60, 30);

			weaponButton.setOnAction(event -> {
				int laneNumber = UserInputLane();
				Lane lane = game.getOriginalLanes().get(laneNumber - 1);
				try {
					if (game.getResourcesGathered() < weaponRegistry.getPrice()) {
						// Display error message for insufficient resources
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Insufficient Resources");
						alert.setHeaderText("You don't have enough resources to buy this weapon.");
						alert.setContentText("Please select a different weapon or earn more resources.");
						alert.showAndWait();
					} else {
						// Attempt to purchase the weapon
						game.purchaseWeapon(weaponRegistry.getCode(), lane);

						// Update resources and UI after successful purchase
						updateGameViews(); // Update game views after purchase

						// Trigger AI actions
						automatePlayerActions();
					}
				} catch (InvalidLaneException e) {
					// Handle invalid lane selection
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Invalid Lane Selection");
					alert.setHeaderText("Please select a valid lane to place the weapon.");
					alert.showAndWait();
				} catch (InsufficientResourcesException e) {
					// Handle insufficient resources for weapon purchase
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Insufficient Resources");
					alert.setHeaderText("You don't have enough resources to buy this weapon.");
					alert.setContentText("Please select a different weapon or earn more resources.");
					alert.showAndWait();
				}
			});

			weaponBox.getChildren().addAll(weaponLabel, weaponButton);
			weaponButtonsContainer.getChildren().add(weaponBox);
		}
	}

	// Update the gameOver method to automatically restart the game or exit to the
	// menu
	private void gameOver() {
		VBox gameOverBox = new VBox(20);
		gameOverBox.setAlignment(Pos.CENTER);
		gameOverBox.setStyle("-fx-background-color: black;"); // Set background color to black

		Label gameOverLabel = new Label("Game Over!");
		gameOverLabel.setStyle("-fx-font-size: 50px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");

		Label finalScoreLabel = new Label("Final Score: " + game.getScore());
		finalScoreLabel.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;"); // Change text
																										// color to
																										// white for
																										// contrast

		Button restartButton = createStyledButton("Restart Game", 180, 40);
		restartButton.setOnAction(event -> setupInitialScreen()); // Restart the game

		Button exitButton = createStyledButton("Exit to Menu", 180, 40);
		exitButton.setOnAction(event -> {
			// Show a confirmation dialog before exiting
			Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
			confirmation.setTitle("Exit Game");
			confirmation.setHeaderText("Are you sure you want to exit?");
			confirmation.setContentText("This will take you back to the main menu.");
			ButtonType yesButton = new ButtonType("Yes");
			ButtonType noButton = new ButtonType("No");
			confirmation.getButtonTypes().setAll(yesButton, noButton);
			confirmation.showAndWait().ifPresent(response -> {
				if (response == yesButton) {
					setupInitialScreen();
				}
			});
		});

		gameOverBox.getChildren().addAll(gameOverLabel, finalScoreLabel, restartButton, exitButton);

		// Create a scene with the game over box and set it on the stage
		Scene gameOverScene = new Scene(gameOverBox, 1000, 600); // Adjust the size as needed
		primaryStage.setScene(gameOverScene); // Assuming 'primaryStage' is your main stage reference
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	public static void main(String[] args) {
		launch(args);
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
// Graphics_Setup:

// Modify the method UserInputLane to return the selected lane from the ComboBox
	public int UserInputLane() {
		ObservableList<Integer> laneNumbers = FXCollections.observableArrayList();
		for (int i = 1; i <= game.getOriginalLanes().size(); i++) {
			laneNumbers.add(i);
		}

		ComboBox<Integer> laneComboBox = new ComboBox<>(laneNumbers);
		laneComboBox.setPromptText("Select Lane");

		HBox container = new HBox();
		container.getChildren().add(laneComboBox);

		Alert laneSelectionAlert = new Alert(Alert.AlertType.CONFIRMATION);
		laneSelectionAlert.setTitle("Select Lane");
		laneSelectionAlert.setHeaderText("Choose a Lane");
		laneSelectionAlert.getDialogPane().setContent(container);

		Optional<ButtonType> result = laneSelectionAlert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			return laneComboBox.getValue();
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Invalid Lane Number");
			alert.setHeaderText("Invalid input! Please enter a valid lane number.");
			alert.showAndWait();
			return UserInputLane(); // Recursive call until valid input
		}
	}

// For Weapon Images
	public String WeaponBought(Weapon weapon) {
		String imageName = "";

		switch (weapon.getClass().getSimpleName()) {
		case "PiercingCannon":
			imageName = "Piercing Canon.png";
			break;
		case "SniperCannon":
			imageName = "Sniper Canon.png";
			break;
		case "VolleySpreadCannon":
			imageName = "Volley Spread Cannon.png";
			break;
		case "WallTrap":
			imageName = "wall trap.png";
			break;
		default:
// Handle default case (unknown Weapon type)
			break;
		}

		return imageName;
	}

// For Titans Images
	public String TitanImage(Titan titan) {
		String imageName = "";

		switch (titan.getClass().getSimpleName()) {
		case "PureTitan":
			imageName = "pure titan.png";
			break;
		case "AbnormalTitan":
			imageName = "abnormal titan.png";
			break;
		case "ArmoredTitan":
			imageName = "armored titan.png";
			break;
		case "ColossalTitan":
			imageName = "colossal titan.png";
			break;
		default:
// Handle default case (unknown Titan type)
			break;
		}

		return imageName;
	}

	// Update the playIntroVideo method to automatically skip the video after
	// completion
	private void playIntroVideo() {
		mediaPlayer.stop();

		String path = "src/Video.mp4";
		Media videoMedia = new Media(new File(path).toURI().toString());
		videoPlayer = new MediaPlayer(videoMedia);
		MediaView mediaView = new MediaView(videoPlayer);

		StackPane videoPane = new StackPane();
		videoPane.getChildren().add(mediaView);

		Label skipLabel = new Label("Press Enter To Skip The Video");
		skipLabel.setStyle("-fx-font-size: 20px; " + "-fx-font-weight: bold; " + "-fx-text-fill: #FFFFFF; ");
		skipLabel.setPadding(new Insets(5));
		DropShadow borderEffect = new DropShadow();
		borderEffect.setColor(Color.web("#0000FF"));
		skipLabel.setEffect(borderEffect);

		StackPane.setAlignment(skipLabel, Pos.BOTTOM_CENTER); // Align label to the bottom center
		StackPane.setMargin(skipLabel, new Insets(0, 0, 20, 0)); // Add bottom margin for spacing

		videoPane.getChildren().add(skipLabel);

		Scene videoScene = new Scene(videoPane, 1000, 625, Color.BLACK);

		videoScene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				videoPlayer.stop();
				showDifficultySelection();
				playBackgroundMusic("src/Attack On Titan.mp3");
			}
		});

		primaryStage.setScene(videoScene);

		videoPlayer.play();
		videoPlayer.setOnEndOfMedia(() -> {
			showDifficultySelection(); // Automatically proceed after video ends
		});
	}

	// Update the setBackgroundImage method to use a fade-in animation
	private void setBackgroundImage(Region region, String imageUrl) {
		Image backgroundImage = new Image(imageUrl);
		BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		region.setBackground(new Background(background));

		// Add fade-in animation for the background image
		FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), region);
		fadeTransition.setFromValue(0);
		fadeTransition.setToValue(1);
		fadeTransition.play();

	}

	private void playBackgroundMusic(String path) {
		try {

			Media media = new Media(new File(path).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setAutoPlay(true);
			mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely
		} catch (Exception e) {
			System.err.println("Error loading background music: " + e.getMessage());
		}
	}

	private Button createStyledButton(String text, double width, double height) {
		Button button = new Button(text);
		button.setPrefSize(width, height);
// Set the initial style with black background and blue border, and blue text
		button.setStyle("-fx-background-color: #000000; " + // Black background
				"-fx-background-radius: 6px; " + "-fx-border-color: #0000FF; " + // Blue border
				"-fx-border-width: 2px; " + "-fx-text-fill: #0000FF;"); // Blue text color
// Change style on mouse hover
		button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #000000; " + // Black background on hover
				"-fx-border-color: #1f1f1f; " + "-fx-text-fill: #1f1f1f;")); // Dark gray text color on hover
// Restore initial style on mouse exit
		button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #000000; " + "-fx-border-color: #0000FF; " + // Blue
// border
				"-fx-text-fill: #0000FF;")); // Blue text color
		return button;
	}

	private void setBackgroundImageFull(Region region, String imageUrl) {
		Image backgroundImage = new Image(imageUrl);
		BackgroundSize backgroundSize = new BackgroundSize(region.getWidth(), region.getHeight(), false, false, false,
				false);
		BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
		region.setBackground(new Background(background));
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Setter & Getters:
	public int getResources() {
		return resources;
	}

	public void setResources(int resources) {
		this.resources = resources;
	}
}