/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.csaclient.provider.extended.logger;

import java.util.Random;

/**
 *
 * @author bho
 */
public class Lorem {
    private static final String[] IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque hendrerit imperdiet mi quis convallis. Pellentesque fringilla imperdiet libero, quis hendrerit lacus mollis et. Maecenas porttitor id urna id mollis. Suspendisse potenti. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras lacus tellus, semper hendrerit arcu quis, auctor suscipit ipsum. Vestibulum venenatis ante et nulla commodo, ac ultricies purus fringilla. Aliquam lectus urna, commodo eu quam a, dapibus bibendum nisl. Aliquam blandit a nibh tincidunt aliquam. In tellus lorem, rhoncus eu magna id, ullamcorper dictum tellus. Curabitur luctus, justo a sodales gravida, purus sem iaculis est, eu ornare turpis urna vitae dolor. Nulla facilisi. Proin mattis dignissim diam, id pellentesque sem bibendum sed. Donec venenatis dolor neque, ut luctus odio elementum eget. Nunc sed orci ligula. Aliquam erat volutpat.".split(" ");
    private static final int MSG_WORDS = 8;
    private int idx = 0;

    private final Random random = new Random(42);

    synchronized public String nextString() {
        int end = Math.min(idx + MSG_WORDS, IPSUM.length);

        StringBuilder result = new StringBuilder();
        for (int i = idx; i < end; i++) {
            result.append(IPSUM[i]).append(" ");
        }

        idx += MSG_WORDS;
        idx = idx % IPSUM.length;

        return result.toString();
    }

    synchronized public Level nextLevel() {
        double v = random.nextDouble();

        if (v < 0.8) {
            return Level.DEBUG;
        }

        if (v < 0.95) {
            return Level.INFO;
        }

        if (v < 0.985) {
            return Level.WARN;
        }

        return Level.ERROR;
    }
}

