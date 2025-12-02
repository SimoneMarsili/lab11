package it.unibo.oop.reactivegui02;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

/**
 * Second example of reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final JButton stopButton = new JButton("stop");
    private final JButton up = new JButton("UP");
    private final JButton down = new JButton("DOWN");

    /**
     * Builds a new CGUI.
     */
    public ConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stopButton);
        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();
        final Thread counter = new Thread(agent);
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
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
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
            disableAllButtons(up, down, stopButton);
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
    }
}
