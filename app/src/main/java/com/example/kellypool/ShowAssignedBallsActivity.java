package com.example.kellypool;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowAssignedBallsActivity extends AppCompatActivity {

    private List<String> playerNames;
    private int ballsPerPlayer;
    private int currentPlayerIndex = 0;
    private List<Integer> shuffledBalls = new ArrayList<>();
    private Map<Integer, String> ballAssignments = new HashMap<>(); // New map to assign balls to players

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_assigned_balls);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playerNames = getIntent().getStringArrayListExtra("playerNames");
        ballsPerPlayer = getIntent().getIntExtra("ballsPerPlayer", 1); // Default to 1 ball per player

        generateAndShuffleBalls();
        assignBallsToPlayers(); // Assign balls to players at the start

        // Find views by their IDs
        TextView playerNameTextView = findViewById(R.id.playerName);
        GridLayout ballsGrid = findViewById(R.id.ballsGrid);
        Button nextButton = findViewById(R.id.nextButton);
        Button confirmPlayerButton = findViewById(R.id.btn_confirm_player);

        updateConfirmationButton(confirmPlayerButton);

        confirmPlayerButton.setOnClickListener(view -> {
            showPlayerBalls(playerNameTextView, ballsGrid);
            confirmPlayerButton.setVisibility(View.GONE);
            ballsGrid.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
        });

        nextButton.setOnClickListener(view -> {
            currentPlayerIndex++;
            if (currentPlayerIndex < playerNames.size()) {
                // Reset UI for the next player
                ballsGrid.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                confirmPlayerButton.setVisibility(View.VISIBLE);

                updateConfirmationButton(confirmPlayerButton);
            } else {
                // Finish activity when all players have been shown
                launchGameActivity();
            }
        });
    }

    private void updateConfirmationButton(Button confirmPlayerButton) {
        String playerName = playerNames.get(currentPlayerIndex);
        confirmPlayerButton.setText("I am " + playerName);
    }

    private void showPlayerBalls(TextView playerNameTextView, GridLayout ballsGrid) {
        String playerName = playerNames.get(currentPlayerIndex);
        List<Integer> assignedBalls = getBallsForPlayer(playerName);

        // Update the UI with player name
        playerNameTextView.setText("Player: " + playerName);

        // Clear previous images
        ballsGrid.removeAllViews();

        // Populate the GridLayout with images
        for (int ball : assignedBalls) {
            ImageView ballImage = new ImageView(this);
            int imageResId = getResources().getIdentifier("billard_" + ball, "drawable", getPackageName());

            if (imageResId == 0) {
                Log.e("ShowAssignedBallsActivity", "Image not found for billiard_" + ball);
            } else {
                ballImage.setImageResource(imageResId);
            }

            // Set layout parameters for fixed size and spacing
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = getResources().getDimensionPixelSize(R.dimen.ball_image_size);
            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.ball_image_size);
            layoutParams.setMargins(8, 8, 8, 8); // Add spacing between items

            ballImage.setLayoutParams(layoutParams);
            ballImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ballsGrid.addView(ballImage);
        }
    }

    private void generateAndShuffleBalls() {
        shuffledBalls.clear();
        for (int i = 1; i <= 15; i++) {
            shuffledBalls.add(i);
        }
        Collections.shuffle(shuffledBalls);
    }

    private void assignBallsToPlayers() {
        ballAssignments.clear();
        int ballIndex = 0;

        for (String playerName : playerNames) {
            for (int i = 0; i < ballsPerPlayer; i++) {
                if (ballIndex < shuffledBalls.size()) {
                    ballAssignments.put(shuffledBalls.get(ballIndex), playerName);
                    ballIndex++;
                }
            }
        }

        // Remaining balls are considered free balls
        for (int i = ballIndex; i < shuffledBalls.size(); i++) {
            ballAssignments.put(shuffledBalls.get(i), "Free Ball");
        }

        // Debugging: Log the ball assignments
        Log.d("ShowAssignedBallsActivity", "Ball Assignments: " + ballAssignments);
    }

    private List<Integer> getBallsForPlayer(String playerName) {
        List<Integer> balls = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : ballAssignments.entrySet()) {
            if (entry.getValue().equals(playerName)) {
                balls.add(entry.getKey());
            }
        }
        return balls;
    }

    private void launchGameActivity() {
        Intent intent = new Intent(ShowAssignedBallsActivity.this, GameActivity.class);
        intent.putStringArrayListExtra("playerNames", new ArrayList<>(playerNames));
        intent.putExtra("ballAssignments", new HashMap<>(ballAssignments)); // Pass the ball assignments
        intent.putExtra("ballsPerPlayer",ballsPerPlayer);
        startActivity(intent);

        finish(); // Close the current activity
    }
}
