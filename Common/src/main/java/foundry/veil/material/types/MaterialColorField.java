package foundry.veil.material.types;

import foundry.veil.color.Color;
import foundry.veil.material.IMaterialField;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MaterialColorField implements IMaterialField<Color> {

    private Color color;

    private TreeMap<String, MaterialSliderField> sliders;

    public MaterialColorField(Color color) {
        this.sliders = new TreeMap<>();
        this.sliders.put("r", new MaterialSliderField(color.getRed(), 0, 255, 1));
        this.sliders.put("g", new MaterialSliderField(color.getGreen(), 0, 255, 1));
        this.sliders.put("b", new MaterialSliderField(color.getBlue(), 0, 255, 1));
        this.sliders.put("a", new MaterialSliderField(color.getAlpha(), 0, 255, 1));
        this.color = new Color((float)sliders.get("r").getValue(),(float) sliders.get("g").getValue(), (float)sliders.get("b").getValue(), (float)sliders.get("a").getValue());
    }

    public MaterialColorField(){
        this.sliders = new TreeMap<>();
        this.sliders.put("r", new MaterialSliderField(0, 0, 255, 1));
        this.sliders.put("g", new MaterialSliderField(0, 0, 255, 1));
        this.sliders.put("b", new MaterialSliderField(0, 0, 255, 1));
        this.sliders.put("a", new MaterialSliderField(0, 0, 255, 1));
        this.color = new Color((float)sliders.get("r").getValue(),(float) sliders.get("g").getValue(), (float)sliders.get("b").getValue(), (float)sliders.get("a").getValue());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Set<String> getSliderKeys(){
        return new TreeSet<>(sliders.keySet());
    }

    public MaterialSliderField getSlider(String name){
        return sliders.get(name);
    }

    public Set<MaterialSliderField> getSliders(){
        return new TreeSet<>(sliders.values());
    }

    public void setSlider(String name, MaterialSliderField slider){
        sliders.put(name, slider);
    }

    public void setSliders(TreeMap<String, MaterialSliderField> sliders){
        this.sliders = sliders;
    }

    public void updateColor(){
        this.color = new Color((float)sliders.get("r").getValue(),(float) sliders.get("g").getValue(), (float)sliders.get("b").getValue(), (float)sliders.get("a").getValue());
    }

    @Override
    public Color getValue() {
        return null;
    }

    @Override
    public void setValue(Color value) {

    }

    @Override
    public MaterialFieldType getType() {
        return null;
    }
}
