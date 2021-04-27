package com.github.easyware.easyapiapp;

import com.github.easyware.easyapiapp.object.SourceConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.util.List;

@Component
public class EasyApiTask implements InitializingBean {
    private static Logger logger= LoggerFactory.getLogger(EasyApiTask.class);
    @Autowired
    SourceMan sourceMan;
    @Autowired
    CommentParser commentParser;
    @Value("${saveRoot}")
    String saveRoot;

    @Override
    public void afterPropertiesSet() throws Exception {
        commentParser.initGroups(sourceMan.getGroupMap().keySet());
    }
   // @Scheduled(fixedDelay = 1000*180)
    public void svnUpdate() throws Exception{
        logger.info("update svn files");

        sourceMan.getGroupMap().forEach((group, sourcesConf)->{
            for(SourceConf sourceConf : sourcesConf) {

                try {
                    List<File> files = sourceMan.update(group, sourceConf);
                    for(File file :files){
                        commentParser.parseFile(group,file);
                    }

                } catch (SVNException e) {
                    logger.error("svn update error",e);
                }
            }
            logger.info("method size="+commentParser.getMethodComments(group).size());
            logger.info("entity size="+commentParser.getClassComments(group).size());

        });

        // task execution logic
    }

   // @Scheduled(fixedDelay = 1000*1800)
    public void fullUpdate() throws Exception{
        logger.info("full update");

        for(String group : sourceMan.getGroupMap().keySet()) {
            commentParser.parseDir(group,new File(saveRoot,group));
            logger.info("method size=" + commentParser.getMethodComments(group).size());
            logger.info("entity size=" + commentParser.getClassComments(group).size());
        }
    }


}
