package com.marshl.alembic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.R.attr.fragment;

public class MainActivity extends FragmentActivity {
    private static final int NUM_PAGES = 2;
    private static final String GAME_NAME_KEY = "GAME_NAME";
    private static final String SELECTED_INGREDIENTS_KEY = "SELECTED_INGREDIENTS";
    private final static String SHARED_PREFERENCE_KEY = "ALEMBIC_SHARED_PREFS";
    //public IngredientListAdapter ingredientListAdapter;
    public AlchemyGame currentGame;
    private List<AlchemyGame> gameMap;
    //private ExpandableListView ingredientListView;
    private ViewPager viewPager;
    private FragmentStatePagerAdapter pagerAdapter;
    private EffectListFragment effectListFragment;
    private IngredientListFragment ingredientListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.viewPager = (ViewPager) this.findViewById(R.id.pager);
        this.pagerAdapter = new IngredientListPagerAdapter(this.getSupportFragmentManager());
        this.viewPager.setAdapter(this.pagerAdapter);

        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                Fragment fragment = null;

                if (position == 0 && MainActivity.this.ingredientListFragment != null) {
                    fragment = MainActivity.this.ingredientListFragment;
                } else if (position == 1 && MainActivity.this.effectListFragment != null) {
                    fragment = MainActivity.this.effectListFragment;
                }

                if (fragment != null) {
                    MainActivity.this.currentGame.recalculateIngredientEffects();
                    fragment.getArguments().remove(AlchemyGame.ALCHEMY_GAME_PARCEL_NAME);
                    fragment.getArguments().putParcelable(AlchemyGame.ALCHEMY_GAME_PARCEL_NAME, MainActivity.this.currentGame);

                    if (fragment == MainActivity.this.effectListFragment) {
                        MainActivity.this.effectListFragment.refreshEffects();
                    } else if (fragment == MainActivity.this.ingredientListFragment) {
                        MainActivity.this.ingredientListFragment.refreshIngredients();
                    }
                }

                MainActivity.this.pagerAdapter.notifyDataSetChanged();
                Log.d("!!!", "onPageSelected: " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        try {
            AlchemyXmlParser parser = new AlchemyXmlParser();
            this.gameMap = parser.parseXml(this);
        } catch (IOException | XmlPullParserException ex) {
            throw new RuntimeException(ex);
        }


        SharedPreferences settings = this.getSharedPreferences(SHARED_PREFERENCE_KEY, 0);

        String gameName = settings.getString(GAME_NAME_KEY, null);
        if (gameName != null) {
            this.currentGame = gameName.equals("mw") ? this.gameMap.get(0) : this.gameMap.get(1);
        } else {
            this.currentGame = this.gameMap.get(0);
        }

        Set<String> selectedIngredients = settings.getStringSet(SELECTED_INGREDIENTS_KEY, null);
        if (selectedIngredients != null) {
            for (AlchemyPackage pack : this.currentGame.packages) {
                for (Ingredient ingredient : pack.ingredients) {
                    if (selectedIngredients.contains(ingredient.getName())) {
                        ingredient.selected = true;
                    }
                }
            }
        }

/*
        this.ingredientListView = (ExpandableListView) this.findViewById(R.id.ingredient_listview);
        this.ingredientListAdapter = new IngredientListAdapter(this, this.currentGame);
        this.ingredientListView.setAdapter(this.ingredientListAdapter);

        this.ingredientListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                IngredientListAdapter adapter = (IngredientListAdapter) parent.getExpandableListAdapter();
                Object obj = adapter.getChild(groupPosition, childPosition);

                Ingredient ingredient = (Ingredient) obj;
                ingredient.selected = !ingredient.selected;

                adapter.notifyDataSetChanged();

                int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
                parent.setItemChecked(index, true);
                return true;
            }
        });*/
    }

    @Override
    public void onDestroy() {

        SharedPreferences settings = this.getSharedPreferences(SHARED_PREFERENCE_KEY, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(GAME_NAME_KEY, this.currentGame.getPrefix());

        Set<String> selectedIngredients = new HashSet<>();
        for (AlchemyPackage pack : this.currentGame.packages) {
            for (Ingredient ingred : pack.ingredients) {
                if (ingred.selected) {
                    selectedIngredients.add(ingred.getName());
                }
            }
        }
        editor.putStringSet(SELECTED_INGREDIENTS_KEY, selectedIngredients);

        editor.apply();
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (this.viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            this.viewPager.setCurrentItem(this.viewPager.getCurrentItem() - 1);
        }
    }

    public void switchGame() {
        this.currentGame = this.currentGame == this.gameMap.get(0) ? this.gameMap.get(1) : this.gameMap.get(0);
        this.ingredientListAdapter.setGame(this.currentGame);
        this.ingredientListView.setScrollY(0);
        this.ingredientListView.setScrollY(0);
        for (int i = 0; i < this.ingredientListAdapter.getGroupCount(); ++i) {
            this.ingredientListView.collapseGroup(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ingredient_menu, menu);
        return true;
    }

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String switchGameTitle = this.currentGame.getPrefix().equals("mw") ? "Oblivion" : "Morrowind";
        switchGameTitle = "Switch to " + switchGameTitle;
        MenuItem switchGameItem = menu.findItem(R.id.switch_game);
        switchGameItem.setTitle(switchGameTitle);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_game: {
                this.switchGame();
                return true;
            }
            case R.id.remove_ingredients: {
                this.currentGame.removeAllIngredients();
                this.ingredientListAdapter.notifyDataSetChanged();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void onMixIngredientButtonDown(View view) {
        this.currentGame.recalculateIngredientEffects();
        Intent intent = new Intent(this, EffectListActivity.class);
        intent.putExtra(AlchemyGame.ALCHEMY_GAME_PARCEL_NAME, this.currentGame);
        startActivity(intent);
    }

    private class IngredientListPagerAdapter extends FragmentStatePagerAdapter {
        public IngredientListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("MainActivity", "Page " + position);
            switch (position) {
                case 0:
                    MainActivity.this.ingredientListFragment = IngredientListFragment.newInstance(MainActivity.this.currentGame);
                    return MainActivity.this.ingredientListFragment;
                case 1:
                    MainActivity.this.effectListFragment = EffectListFragment.newInstance(MainActivity.this.currentGame);
                    return MainActivity.this.effectListFragment;
                default:
                    throw new IllegalArgumentException("Unknown position " + position);

            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
