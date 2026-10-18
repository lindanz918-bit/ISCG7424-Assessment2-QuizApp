package cs.linda.iscg7424assessment2quizapp.player;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cs.linda.iscg7424assessment2quizapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerQuizzesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerQuizzesFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public PlayerQuizzesFragment() {
        // Required empty public constructor
    }
    public static PlayerQuizzesFragment newInstance(String param1, String param2) {
        PlayerQuizzesFragment fragment = new PlayerQuizzesFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_quizzes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new QuizPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Ongoing");
            } else if (position == 1) {
                tab.setText("Upcoming");
            } else {
                tab.setText("Past");
            }

        }).attach();
    }

    private static class QuizPagerAdapter extends FragmentStateAdapter {
        public QuizPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String type;
            if (position == 0) {
                type = "ongoing";
            } else if (position == 1) {
                type = "upcoming";
            } else {
                type = "past";
            }
            return QuizListTabFragment.newInstance(type);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}