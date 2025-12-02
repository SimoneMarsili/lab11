package it.unibo.oop.reactivegui03;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;
//import it.unibo.oop.reactivegui03.AnotherConcurrentGUI.Agent.TimeAgent;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final int TIME_OUT = 10_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final JButton stopButton = new JButton("stop");
    private final JButton up = new JButton("UP");
    private final JButton down = new JButton("DOWN");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stopButton);
        this.getContentPane().add(panel);

        final Agent agent = new Agent();
        final Agent.TimeAgent timeAgent = agent.new TimeAgent();
        final Thread counter = new Thread(agent);
        final Thread timer = new Thread(timeAgent);
        /*
         * Register a listener that stops it
         */
        stopButton.addActionListener(e -> {
            agent.stopCounting();
        });

        up.addActionListener(e -> {
            agent.goUp();
            if (!counter.isAlive()) {
                counter.start();
            }
        });

        down.addActionListener(e -> {
            agent.goDown();
            if (!counter.isAlive()) {
                counter.start();
            }
        });

        this.setVisible(true);
        timer.start();
    }

    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility.
         */
        private volatile boolean stop;
        private volatile boolean goingUp;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (this.goingUp) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            disableAllButtons(down, up, stopButton);
        }

        void disableAllButtons(final JButton... buttons) {
            SwingUtilities.invokeLater(() -> {
                for (final JButton b : buttons) {
                    b.setEnabled(false);
                }
            });
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void goUp() {
            this.goingUp = true;
        }

        public void goDown() {
            this.goingUp = false;
        }

        private final class TimeAgent implements Runnable {

            @Override
            public void run() {
                try {
                    Thread.sleep(TIME_OUT); // attende 10 secondi
                } catch (final InterruptedException e) {
                    return; // timer interrotto → esco
                }

                stopCounting();
                disableAllButtons(up, down, stopButton);
            }
        }
    }
}
