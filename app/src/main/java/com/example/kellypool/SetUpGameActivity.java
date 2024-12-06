package com.example.kellypool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetUpGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_up_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner playersSpinner = findViewById(R.id.playersSpinner);
        Spinner ballsSpinner = findViewById(R.id.ballsSpinner);
        Button confirmButton = findViewById(R.id.btn_confirm);

        Map<Integer, Integer> playerToMaxBalls = new HashMap<>();
        playerToMaxBalls.put(2, 6);
        playerToMaxBalls.put(3, 5);
        playerToMaxBalls.put(4, 3);
        playerToMaxBalls.put(5, 3);
        playerToMaxBalls.put(6, 2);
        playerToMaxBalls.put(7, 2);
        playerToMaxBalls.put(8, 1);
        playerToMaxBalls.put(9, 1);

        List<String> playersList = new ArrayList<>();
        for (int i = 2; i <= 9; i++) {
            playersList.add(i + " Players");
        }
        ArrayAdapter<String> playersAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, playersList);
        playersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playersSpinner.setAdapter(playersAdapter);



        playersSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int numberOfPlayers = position + 2; // Players range from 2 to 9
                List<String> ballsList = new ArrayList<>();

                // Fetch max balls per player from the map
                int maxBallsPerPlayer = playerToMaxBalls.getOrDefault(numberOfPlayers, 1);

                for (int i = 1; i <= maxBallsPerPlayer; i++) {
                    ballsList.add(i + " Balls per Player");
                }

                ArrayAdapter<String> ballsAdapter = new ArrayAdapter<>(
                        SetUpGameActivity.this, android.R.layout.simple_spinner_item, ballsList);
                ballsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ballsSpinner.setAdapter(ballsAdapter);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Handle case when no item is selected
            }
        });

        confirmButton.setOnClickListener(view -> {
            int numberOfPlayers = playersSpinner.getSelectedItemPosition() + 2; // Players range from 2 to 9
            int ballsPerPlayer = ballsSpinner.getSelectedItemPosition() + 1;   // Balls range from 1 to maxBalls

            // Start PlayerNameActivity and pass data
            Intent intent = new Intent(SetUpGameActivity.this, PlayerEntryActivity.class);
            intent.putExtra("numberOfPlayers", numberOfPlayers);
            intent.putExtra("ballsPerPlayer", ballsPerPlayer);
            startActivity(intent);
        });
    }
}