package foundry.veil.quasar.emitters.modules.particle.render;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RenderData {
    float scale;
    float pitch;
    float yaw;
    float roll;
    float r;
    float g;
    float b;
    float a;
    List<TrailSettings> trails = new ArrayList<>();
    public RenderData(float scale, float pitch, float yaw, float roll, float r, float g, float b, float a) {
        this.scale = scale;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public float getScale() {
        return scale;
    }
    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public void setR(float r) {
        this.r = r;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setB(float b) {
        this.b = b;
    }

    public void setA(float a) {
        this.a = a;
    }

    public List<TrailSettings> getTrails() {
        return trails;
    }

    public void setRGBA(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public void vectorToRotation(Vec3 vector) {
        this.pitch = (float) Math.asin(vector.y());
        this.yaw = (float) Math.atan2(vector.x(), vector.z());
        this.roll = 0;
    }

    public void addTrails(TrailSettings... trails) {
        this.trails.addAll(List.of(trails));
    }

    public void addTrails(List<TrailSettings> trails){
        this.trails = trails;
    }
}
