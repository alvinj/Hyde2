package com.alvinalexander.hyde2

import java.awt.Color
import javax.swing.SwingUtilities
import com.devdaily.heidi.CurtainFrame
import com.devdaily.heidi.DragWindowListener
import javax.swing.JPanel

/**
 * When Hyde is started, show a window.
 */
object Hyde2 extends App {

    new Hyde2
    def getDefaultColor: Color = new Color(60,60,60,255)

}

class Hyde2 {
    
    val color = Hyde2.getDefaultColor
    val curtainPanel = new JPanel
    val frame = new CurtainFrame(null, curtainPanel, color, this)
    val mml = new DragWindowListener(curtainPanel)
    curtainPanel.addMouseListener(mml)
    curtainPanel.addMouseMotionListener(mml)

    SwingUtilities.invokeLater(new Runnable {
        def run {
            frame.display
        }
    })

}



