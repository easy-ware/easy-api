package com.github.easyware.easyapiapp;

import com.github.easyware.easyapiapp.object.SourceConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc2.SvnCleanup;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SourceMan implements InitializingBean {
    private static Logger logger= LoggerFactory.getLogger(SourceMan.class);
    //SVNClientManager svnClientManager;
    //
    @Autowired
    private Environment env;
    @Autowired
    private SvnClient svnClient;
    Map<String, SourceConf[]>  groupMap=new ConcurrentHashMap<>();
    Map<String,SVNClientManager> manMap =new ConcurrentHashMap<>();
    @Value("${saveRoot}")
    String saveRoot;

    public Map<String, SourceConf[]> getGroupMap() {
        return groupMap;
    }



    @Override
    public void afterPropertiesSet() throws Exception {

        String prop= env.getProperty("groups");
        if(prop==null){
            logger.warn("'groups' not found in config");
            return;
        }
        String[] groups=prop.split(",");
        for(String group:groups){
            prop=env.getProperty(group+".sources");
            String[] sources=prop.split(",");

            SourceConf[] sourceConfs =new SourceConf[sources.length];
            for(int i=0;i<sources.length;i++){
                SourceConf sourceConf =new SourceConf();
                sourceConf.setName(sources[i]);
                String[] items= env.getProperty(sources[i]).split(",");
                sourceConf.setUrl(items[0]);
                sourceConf.setUsername(items[1]);
                sourceConf.setPassword(items[2]);
                sourceConfs[i]= sourceConf;
            }

            groupMap.put(group, sourceConfs);
        }


    }




    public List<File> update(String group, SourceConf sourceConf) throws SVNException {
         return svnClient.update(group,sourceConf);

    }


}
