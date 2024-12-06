package com.example.kellypool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class PlayerEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int numberOfPlayers = getIntent().getIntExtra("numberOfPlayers", 2); // Default to 2
        int ballsPerPlayer = getIntent().getIntExtra("ballsPerPlayer", 1);

        LinearLayout playerNamesContainer = findViewById(R.id.playerNameContainer);
        Button submitButton = findViewById(R.id.btnSubmit);

        // Dynamically generate input fields for player names
        List<EditText> playerNameInputs = new ArrayList<>();
        for (int i = 1; i <= numberOfPlayers; i++) {
            EditText playerNameInput = new EditText(this);
            playerNameInput.setHint("Player " + i + " Name");

            // Set layout params with margin for spacing
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 10, 0, 10); // Add 8dp margin top and bottom
            playerNameInput.setLayoutParams(layoutParams);

            playerNamesContainer.addView(playerNameInput);
            playerNameInputs.add(playerNameInput);
        }

        submitButton.setOnClickListener(view -> {
            List<String> playerNames = new ArrayList<>();
            for (EditText editText : playerNameInputs) {
                String name = editText.getText().toString().trim();
                if (!name.isEmpty()) {
                    playerNames.add(name);
                } else {
                    playerNames.add("Player " + (playerNames.size() + 1)); // Default name
                }
            }

            Intent intent = new Intent(PlayerEntryActivity.this, ShowAssignedBallsActivity.class);
            intent.putStringArrayListExtra("playerNames", new ArrayList<>(playerNames));
            intent.putExtra("ballsPerPlayer", ballsPerPlayer);
            startActivity(intent);
        });
    }
}