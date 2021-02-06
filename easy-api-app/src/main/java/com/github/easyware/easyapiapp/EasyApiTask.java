package com.github.easyware.easyapiapp;

import com.github.easyware.easyapiapp.object.SvnSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNException;

import java.awt.peer.ComponentPeer;
import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class EasyApiTask {
    private static Logger logger= LoggerFactory.getLogger(EasyApiTask.class);
    @Autowired
    SvnClient svnClient;
    @Autowired
    CommentParser commentParser;
    @Value("${saveRoot}")
    String saveRoot;
    @Scheduled(fixedDelay = 1000*10)
    public void svnUpdate() throws Exception{
        logger.info("update svn files");

         svnClient.getGroupMap().forEach((group,svnSources)->{
             for(SvnSource svnSource:svnSources) {
                 try {
                     List<File> files = svnClient.update(group, svnSource);
                     for(File file :files){
                         commentParser.parseFile(file);
                     }

                 } catch (SVNException e) {
                     throw  new RuntimeException(e);
                 }
             }

         });
        logger.info("method size="+commentParser.getMethodComments().size());
        logger.info("entity size="+commentParser.getFieldComments().size());
        // task execution logic
    }

    @Scheduled(fixedDelay = 1000*10)
    public void fullUpdate() throws Exception{
        logger.info("full update");
        commentParser.parseDir(new File(saveRoot));
        logger.info("method size="+commentParser.getMethodComments().size());
        logger.info("entity size="+commentParser.getFieldComments().size());
    }
}
