package cs.linda.iscg7424assessment2quizapp.player;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import cs.linda.iscg7424assessment2quizapp.R;

public class PlayerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player_main_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.playerBottomNav);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.playerFragmentContainer, new PlayerQuizzesFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_player_quizzes) {
                selectedFragment = new PlayerQuizzesFragment();
            } else if (itemId == R.id.nav_player_history) {
                selectedFragment = new PlayerHistoryFragment();
            } else if (itemId == R.id.nav_player_profile) {
//                selectedFragment = new PlayerProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.playerFragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}