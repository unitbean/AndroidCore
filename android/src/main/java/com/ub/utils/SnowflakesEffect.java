/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package com.ub.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowflakesEffect {

    private final Paint particlePaint;
    private final Paint particleThinPaint;
    private final Random random = new Random();
    private final Interpolator accelerateInterpolator = new AccelerateInterpolator();
    private final Interpolator decelerateInterpolator = new DecelerateInterpolator();
    private final int statusBarHeight;
    private final float density;

    private long lastAnimationTime;

    final float angleDiff = (float) (Math.PI / 180 * 60);

    private class Particle {
        float x;
        float y;
        float vx;
        float vy;
        float velocity;
        float alpha;
        float lifeTime;
        float currentTime;
        float scale;
        int type;

        public void draw(Canvas canvas) {
            switch (type) {
                case 0: {
                    particlePaint.setAlpha((int) (255 * alpha));
                    canvas.drawPoint(x, y, particlePaint);
                    break;
                }
                case 1:
                default: {
                    particleThinPaint.setAlpha((int) (255 * alpha));

                    float angle = (float) -Math.PI / 2;

                    float px = dpf2(2.0f) * 2 * scale;
                    float px1 = -dpf2(0.57f) * 2 * scale;
                    float py1 = dpf2(1.55f) * 2 * scale;
                    for (int a = 0; a < 6; a++) {
                        float x1 = (float) Math.cos(angle) * px;
                        float y1 = (float) Math.sin(angle) * px;
                        float cx = x1 * 0.66f;
                        float cy = y1 * 0.66f;
                        canvas.drawLine(x, y, x + x1, y + y1, particleThinPaint);

                        float angle2 = (float) (angle - Math.PI / 2);
                        x1 = (float) (Math.cos(angle2) * px1 - Math.sin(angle2) * py1);
                        y1 = (float) (Math.sin(angle2) * px1 + Math.cos(angle2) * py1);
                        canvas.drawLine(x + cx, y + cy, x + x1, y + y1, particleThinPaint);

                        x1 = (float) (-Math.cos(angle2) * px1 - Math.sin(angle2) * py1);
                        y1 = (float) (-Math.sin(angle2) * px1 + Math.cos(angle2) * py1);
                        canvas.drawLine(x + cx, y + cy, x + x1, y + y1, particleThinPaint);

                        angle += angleDiff;
                    }
                    break;
                }
            }

        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> freeParticles = new ArrayList<>();

    /**
     *
     * @param colorParticle - color for main particles
     * @param colorThinParticle - color for secondary particles
     * @param strokeParticle - stroke for particle. Default is 1.5f
     * @param strokeThinParticle - stroke for thin particle. Default is 0.5f
     * @param statusBarHeight - top offset for status bar height
     * @param density - density of device screen
     */
    public SnowflakesEffect(
        @ColorInt int colorParticle,
        @ColorInt int colorThinParticle,
        float strokeParticle,
        float strokeThinParticle,
        int statusBarHeight,
        float density
    ) {
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStrokeWidth(strokeParticle);
        particlePaint.setColor(colorParticle);
        particlePaint.setStrokeCap(Paint.Cap.ROUND);
        particlePaint.setStyle(Paint.Style.STROKE);

        particleThinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particleThinPaint.setStrokeWidth(strokeThinParticle);
        particleThinPaint.setColor(colorThinParticle);
        particleThinPaint.setStrokeCap(Paint.Cap.ROUND);
        particleThinPaint.setStyle(Paint.Style.STROKE);

        this.statusBarHeight = statusBarHeight;
        this.density = density;

        for (int a = 0; a < 20; a++) {
            freeParticles.add(new Particle());
        }
    }

    private void updateParticles(long dt) {
        int count = particles.size();
        for (int a = 0; a < count; a++) {
            Particle particle = particles.get(a);
            if (particle.currentTime >= particle.lifeTime) {
                if (freeParticles.size() < 40) {
                    freeParticles.add(particle);
                }
                particles.remove(a);
                a--;
                count--;
                continue;
            }
            if (particle.currentTime < 200.0f) {
                particle.alpha = accelerateInterpolator.getInterpolation(particle.currentTime / 200.0f);
            } else {
                particle.alpha = 1.0f - decelerateInterpolator.getInterpolation((particle.currentTime - 200.0f) / (particle.lifeTime - 200.0f));
            }
            particle.x += particle.vx * particle.velocity * dt / 500.0f;
            particle.y += particle.vy * particle.velocity * dt / 500.0f;
            particle.currentTime += dt;
        }
    }

    public void onDraw(View parent, Canvas canvas) {
        if (parent == null || canvas == null) {
            return;
        }

        int count = particles.size();
        for (int a = 0; a < count; a++) {
            Particle particle = particles.get(a);
            particle.draw(canvas);
        }

        if (random.nextFloat() > 0.7f && particles.size() < 100) {
            int statusBarHeight = (Build.VERSION.SDK_INT >= 21 ? this.statusBarHeight : 0);
            float cx = random.nextFloat() * parent.getMeasuredWidth();
            float cy = statusBarHeight + random.nextFloat() * (parent.getMeasuredHeight() - ExtensionsKt.dpToPx(parent, 20) - statusBarHeight);

            int angle = random.nextInt(40) - 20 + 90;
            float vx = (float) Math.cos(Math.PI / 180.0 * angle);
            float vy = (float) Math.sin(Math.PI / 180.0 * angle);

            Particle newParticle;
            if (!freeParticles.isEmpty()) {
                newParticle = freeParticles.get(0);
                freeParticles.remove(0);
            } else {
                newParticle = new Particle();
            }
            newParticle.x = cx;
            newParticle.y = cy;

            newParticle.vx = vx;
            newParticle.vy = vy;

            newParticle.alpha = 0.0f;
            newParticle.currentTime = 0;

            newParticle.scale = random.nextFloat() * 1.2f;
            newParticle.type = random.nextInt(2);

            newParticle.lifeTime = 2000 + random.nextInt(100);
            newParticle.velocity = 20.0f + random.nextFloat() * 4.0f;
            particles.add(newParticle);
        }

        long newTime = System.currentTimeMillis();
        long dt = Math.min(17, newTime - lastAnimationTime);
        updateParticles(dt);
        lastAnimationTime = newTime;
        parent.invalidate();
    }

    private float dpf2(float value) {
        if (value == 0) {
            return 0;
        }
        return density * value;
    }
}
