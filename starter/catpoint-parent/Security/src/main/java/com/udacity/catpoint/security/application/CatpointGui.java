package com.udacity.catpoint.security.application;

import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.security.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * This is the primary JFrame for the application that contains all the top-level JPanels.
 *
 * We're not using any dependency injection framework, so this class also handles constructing
 * all our dependencies and providing them to other classes as necessary.
 */
public class CatpointGui extends JFrame {
    private transient SecurityRepository securityRepository = new PretendDatabaseSecurityRepositoryImpl();
    private transient FakeImageService imageService = new FakeImageService();
    private transient SecurityService securityService = new SecurityService(securityRepository, imageService);
    private DisplayPanel displayPanel = new DisplayPanel(securityService);
    private final ControlPanel controlPanel;
    private final SensorPanel sensorPanel;
    private ImagePanel imagePanel = new ImagePanel(securityService);

    public CatpointGui() {
        sensorPanel = new SensorPanel(securityService);
        controlPanel = new ControlPanel(securityService, sensorPanel);
        setLocation(100, 100);
        setSize(600, 850);
        setTitle("Very Secure App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new MigLayout());
        mainPanel.add(displayPanel, "wrap");
        mainPanel.add(imagePanel, "wrap");
        mainPanel.add(controlPanel, "wrap");
        mainPanel.add(sensorPanel);

        getContentPane().add(mainPanel);

    }
}
