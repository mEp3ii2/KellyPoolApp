package com.example.kellypool;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

    private List<String> playerNames;
    private Map<String, Integer> playerBallCount = new HashMap<>();
    private Map<Integer, String> ballAssignments = new HashMap<>();
    private Map<String, TextView> playerTextViews = new HashMap<>(); // Track player TextViews
    private int totalPlayers;
    private int ballsPerPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve data passed from the previous activity
        playerNames = getIntent().getStringArrayListExtra("playerNames");
        ballAssignments = (HashMap<Integer, String>) getIntent().getSerializableExtra("ballAssignments");
        ballsPerPlayer = getIntent().getIntExtra("ballsPerPlayer",1);

        totalPlayers = playerNames.size();

        // Initialize playerBallCount map
        initializePlayerBallCount();

        // Debugging: Log the received data
        Log.d("GameActivity", "Player Names: " + playerNames);
        Log.d("GameActivity", "Ball Assignments: " + ballAssignments);
        Log.d("GameActivity", "Player Ball Count: " + playerBallCount);

        // Populate the player information and ball grid
        populatePlayerInfo();
        populateBallGrid();

        // Handle the New Game button
        Button newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(v -> restartGame());

        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Ensure no duplicate activities
            startActivity(intent);
            finish(); // Close the current activity
        });
    }

    private void initializePlayerBallCount() {
        for (String playerName : playerNames) {
            playerBallCount.put(playerName, 0);
        }

        for (Map.Entry<Integer, String> entry : ballAssignments.entrySet()) {
            String playerName = entry.getValue();
            if (!playerName.equals("Free Ball")) {
                playerBallCount.put(playerName, playerBallCount.get(playerName) + 1);
            }
        }
    }

    private void populatePlayerInfo() {
        LinearLayout playersInfo = findViewById(R.id.playersInfo);

        for (String playerName : playerNames) {
            LinearLayout playerRow = new LinearLayout(this);
            playerRow.setOrientation(LinearLayout.HORIZONTAL);
            playerRow.setPadding(8, 8, 8, 8);

            // TextView for displaying player's ball count
            TextView playerTextView = new TextView(this);
            playerTextView.setText(playerName + ": " + playerBallCount.get(playerName) + " balls");
            playerTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            // Store the TextView for dynamic updates
            playerTextViews.put(playerName, playerTextView);

            // Button to check assigned balls
            Button checkBallsButton = new Button(this);
            checkBallsButton.setText("View Balls");
            checkBallsButton.setOnClickListener(v -> showAssignedBallsPopup(playerName));

            // Add TextView and Button to the row
            playerRow.addView(playerTextView);
            playerRow.addView(checkBallsButton);

            playersInfo.addView(playerRow);
        }
    }


    private void populateBallGrid() {
        GridLayout ballsGrid = findViewById(R.id.ballsGrid);
        ballsGrid.removeAllViews();

        for (int i = 1; i <= 15; i++) {
            ImageView ballIcon = new ImageView(this);
            int imageResId = getResources().getIdentifier("billard_" + i, "drawable", getPackageName());
            ballIcon.setImageResource(imageResId);

            // Set layout parameters for size and spacing
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = 150;
            layoutParams.height = 150;
            layoutParams.setMargins(8, 8, 8, 8);
            ballIcon.setLayoutParams(layoutParams);

            // Handle click events for each ball
            int ballNumber = i;
            ballIcon.setOnClickListener(v -> onBallClicked(ballNumber, ballIcon));

            ballsGrid.addView(ballIcon);
        }
    }

    private void onBallClicked(int ballNumber, ImageView ballIcon) {
        String owner = ballAssignments.get(ballNumber);

        if (owner.equals("Free Ball")) {
            showPopup("Free Ball Sunk!", "This ball is free. It does not belong to any player.", ballIcon, null, false);
        } else {
            showPopup(owner + "'s Ball Sunk!", owner + " loses one ball.", ballIcon, owner, true);
        }
    }

    private void showPopup(String title, String message, ImageView ballIcon, String owner, boolean checkElimination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Check if the player will be eliminated
        boolean isEliminated;
        String eliminationMessage = "";

        if (owner != null && checkElimination) {
            int remainingBalls = playerBallCount.get(owner) - 1;

            if (remainingBalls == 0) {
                isEliminated = true;
                eliminationMessage = owner + " is eliminated!";
            } else {
                isEliminated = false;
            }
        } else {
            isEliminated = false;
        }

        // Build the message
        String finalMessage = message;
        if (isEliminated) {
            finalMessage += "\n\n" + eliminationMessage;
        }

        builder.setTitle(title);
        builder.setMessage(finalMessage);
        builder.setCancelable(false);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Update player's ball count if it's not a free ball
            if (owner != null) {
                int remainingBalls = playerBallCount.get(owner) - 1;
                playerBallCount.put(owner, remainingBalls);

                // Update the player's TextView
                TextView playerTextView = playerTextViews.get(owner);
                if (playerTextView != null) {
                    playerTextView.setText(owner + ": " + remainingBalls + " balls");
                }

                // Handle elimination
                if (isEliminated) {
                    totalPlayers--;

                    // Check for a winner
                    if (totalPlayers == 1) {
                        for (String playerName : playerNames) {
                            if (playerBallCount.get(playerName) > 0) {
                                showWinnerPopup(playerName);
                                break;
                            }
                        }
                    }
                }
            }

            // Update UI: Dim the ball and make it unclickable
            ballIcon.setAlpha(0.5f); // Dim the clicked ball
            ballIcon.setClickable(false);
        });

        builder.show();
    }

    private void showWinnerPopup(String playerName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage(playerName + " wins the game!");

        // Button to start a new game
        builder.setPositiveButton("Start New Game", (dialog, which) -> {
            restartGame();
        });

        // Button to exit the activity
        builder.setNegativeButton("Exit", (dialog, which) -> {
            finish(); // Close the current activity
        });

        builder.show();
    }

    private void showEliminationPopup(String playerName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(playerName + " Eliminated");
        builder.setMessage(playerName + " is out of the game!");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showAssignedBallsPopup(String playerName) {
        // Get the player's assigned balls
        List<Integer> assignedBalls = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : ballAssignments.entrySet()) {
            if (entry.getValue().equals(playerName)) {
                assignedBalls.add(entry.getKey());
            }
        }

        // Create the message with the assigned balls
        StringBuilder ballsMessage = new StringBuilder("Assigned Balls: ");
        for (int ball : assignedBalls) {
            ballsMessage.append(ball).append(" ");
        }

        // Show the popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(playerName + "'s Balls");
        builder.setMessage(ballsMessage.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }


    private void restartGame() {

        List<String> shuffledPlayerNames = new ArrayList<>(playerNames);
        Collections.shuffle(shuffledPlayerNames);

        Intent intent = new Intent(this, ShowAssignedBallsActivity.class);
        intent.putStringArrayListExtra("playerNames", new ArrayList<>(shuffledPlayerNames));
        intent.putExtra("ballsPerPlayer", ballsPerPlayer); // Pass balls per player
        startActivity(intent);
        finish();
    }
}
