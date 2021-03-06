package com.marshl.elderalchemy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class IngredientListAdapter extends BaseExpandableListAdapter {

    private final Activity context;

    private AlchemyGame alchemyGame;

    IngredientListAdapter(Activity context, AlchemyGame alchemyGame) {
        super();
        this.context = context;
        this.alchemyGame = alchemyGame;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        final AlchemyPackage alchemyPackage = this.alchemyGame.getPackage(groupPosition);
        return alchemyPackage.ingredients.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final AlchemyPackage alchemyPackage = this.alchemyGame.getPackage(groupPosition);
        final Ingredient ingredient = alchemyPackage.ingredients.get(childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.ingredient_child_item, parent, false);
        }

        convertView.setId(childPosition);
        final TextView textView = convertView.findViewById(R.id.label);
        final ImageView imageView = convertView.findViewById(R.id.icon);
        final CheckBox checkbox = convertView.findViewById(R.id.ingredient_checkbox);

        textView.setText(ingredient.getName());
        textView.setTypeface(null, ingredient.isSelected() ? Typeface.BOLD : Typeface.NORMAL);
        imageView.setImageResource(this.alchemyGame.getIngredientImageResource(ingredient, this.context));
        checkbox.setChecked(ingredient.isSelected());


        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        final AlchemyPackage alchemyPackage = this.alchemyGame.getPackage(groupPosition);
        return alchemyPackage.ingredients.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.alchemyGame.getPackage(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.alchemyGame.getPackageCount();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        final AlchemyPackage alchemyPackage = this.alchemyGame.getPackage(groupPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.ingredient_group_item,
                    parent, false);
        }

        convertView.setId(groupPosition);

        final TextView textView =  convertView.findViewById(R.id.ingredient_group_text);
        final Resources res = context.getResources();
        textView.setText(res.getString(R.string.ingredient_group_title, alchemyPackage.getPackageName()));

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
