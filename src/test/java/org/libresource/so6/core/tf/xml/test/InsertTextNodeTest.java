/**
 * LibreSource
 * Copyright (C) 2004-2008 Artenum SARL / INRIA
 * http://www.libresource.org - contact@artenum.com
 *
 * This file is part of the LibreSource software, 
 * which can be used and distributed under license conditions.
 * The license conditions are provided in the LICENSE.TXT file 
 * at the root path of the packaging that enclose this file. 
 * More information can be found at 
 * - http://dev.libresource.org/home/license
 *
 * Initial authors :
 *
 * Guillaume Bort / INRIA
 * Francois Charoy / Universite Nancy 2
 * Julien Forest / Artenum
 * Claude Godart / Universite Henry Poincare
 * Florent Jouille / INRIA
 * Sebastien Jourdain / INRIA / Artenum
 * Yves Lerumeur / Artenum
 * Pascal Molli / Universite Henry Poincare
 * Gerald Oster / INRIA
 * Mariarosa Penzi / Artenum
 * Gerard Sookahet / Artenum
 * Raphael Tani / INRIA
 *
 * Contributors :
 *
 * Stephane Bagnier / Artenum
 * Amadou Dia / Artenum-IUP Blois
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package org.libresource.so6.core.tf.xml.test;

import fr.loria.ecoo.so6.xml.node.ElementNode;
import fr.loria.ecoo.so6.xml.node.TextNode;
import fr.loria.ecoo.so6.xml.util.XmlUtil;

import junit.framework.TestCase;

import org.libresource.so6.core.WsConnection;
import org.libresource.so6.core.engine.util.FileUtils;
import org.libresource.so6.core.test.util.TestUtil;

import java.io.File;


public class InsertTextNodeTest extends TestCase {
    private String dir;
    private String dir1;
    private String dir2;
    private String xmlFilePath1;
    private String xmlFilePath2;
    private WsConnection ws1;
    private WsConnection ws2;

    public InsertTextNodeTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        dir = FileUtils.createTmpDir().getPath();

        WsConnection[] ws = TestUtil.createWorkspace(dir, 2, true);
        ws1 = ws[0];
        ws2 = ws[1];
        dir1 = ws1.getPath();
        dir2 = ws2.getPath();
        xmlFilePath1 = dir + File.separator + "text.xml";
        xmlFilePath2 = dir + File.separator + "text.xml";

        // init xml file
        FileUtils.createXmlFile("root", xmlFilePath1);
        ws1.updateAndCommit();
        ws2.updateAndCommit();
    }

    public void testTwoTextNodes() throws Exception {
        //assertTrue(dir, FileUtils.compareDir(dir1, dir2));
        //
        XmlUtil.insertNode(xmlFilePath1, "0:0", new TextNode("textnode1"));
        XmlUtil.insertNode(xmlFilePath1, "0:1", new TextNode("textnode2"));

        //
        ws1.updateAndCommit();
        ws2.updateAndCommit();
        assertTrue(dir, FileUtils.compareDir(dir1, dir2));
    }

    public void testTwoTextNodesThenOneElement() throws Exception {
        //assertTrue(dir, FileUtils.compareDir(dir1, dir2));
        //
        XmlUtil.insertNode(xmlFilePath1, "0:0", new TextNode("textnode1"));
        ws1.updateAndCommit();
        XmlUtil.insertNode(xmlFilePath1, "0:1", new TextNode("textnode2"));

        //
        ws1.updateAndCommit();
        ws2.updateAndCommit();
        XmlUtil.insertNode(xmlFilePath1, "0:1", new ElementNode("br"));
        ws1.updateAndCommit();
        ws2.updateAndCommit();
        assertTrue(dir, FileUtils.compareDir(dir1, dir2));
    }

    public void testTwoConcurrentTextNodes() throws Exception {
        //assertTrue(dir, FileUtils.compareDir(dir1, dir2));
        //
        XmlUtil.insertNode(xmlFilePath1, "0:0", new TextNode("textnode1"));
        XmlUtil.insertNode(xmlFilePath2, "0:0", new TextNode("textnode2"));

        //
        ws1.updateAndCommit();
        ws2.updateAndCommit();
        ws1.updateAndCommit();
        assertTrue(dir, FileUtils.compareDir(dir1, dir2));
    }

    // ce test met en evidence la necessite de distinguer deux noeuds textes
    // adjacents
    public void testTwoConcurrentTextNodesThenOneElement()
        throws Exception {
        //assertTrue(dir, FileUtils.compareDir(dir1, dir2));
        //
        XmlUtil.insertNode(xmlFilePath1, "0:0", new TextNode("textnode1"));
        ws1.updateAndCommit();
        XmlUtil.insertNode(xmlFilePath1, "0:1", new ElementNode("br"));
        ws1.updateAndCommit();
        XmlUtil.insertNode(xmlFilePath2, "0:0", new TextNode("textnode2"));
        ws2.updateAndCommit();
        ws1.updateAndCommit();
        assertTrue(dir, FileUtils.compareDir(dir1, dir2));
    }

    public void tearDown() {
        System.out.println(dir1);
    }
}