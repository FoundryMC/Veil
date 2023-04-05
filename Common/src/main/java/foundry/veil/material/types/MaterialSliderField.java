package foundry.veil.material.types;

import foundry.veil.material.IMaterialField;

public class MaterialSliderField implements IMaterialField {

    float value;
    float min;
    float max;
    float step;

    public MaterialSliderField(float value, float min, float max, float step) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public MaterialSliderField() {
        this.value = 0;
        this.min = 0;
        this.max = 100;
        this.step = 1f;
    }

    @Override
    public Object getValue() {
        return value;
    }


    @Override
    public void setValue(Object value) {
        this.value = (float) value;
    }

    @Override
    public MaterialFieldType getType() {
        return MaterialFieldType.SLIDER;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getStep() {
        return step;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public void increment() {
        value += step;
    }

    public void decrement() {
        value -= step;
    }

    public void increment(float amount) {
        value += amount;
    }

    public void decrement(float amount) {
        value -= amount;
    }

    public void reset() {
        value = 0;
    }

    public void set(float percentage){
        value = min + (max - min) * percentage;
    }

}
