package com.github.easyware.easyapiapp;

import com.github.easyware.easyapiapp.object.SvnSource;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SvnClient implements InitializingBean {
    private static Logger logger= LoggerFactory.getLogger(SvnClient.class);
    //SVNClientManager svnClientManager;
    //
    @Autowired
    private Environment env;
    Map<String, SvnSource[]>  groupMap=new ConcurrentHashMap<>();
    Map<String,SVNClientManager> manMap =new ConcurrentHashMap<>();
    @Value("${saveRoot}")
    String saveRoot;

    public Map<String, SvnSource[]> getGroupMap() {
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

            SvnSource[] svnSources=new SvnSource[sources.length];
            for(int i=0;i<sources.length;i++){
                SvnSource svnSource=new SvnSource();
                svnSource.setName(sources[i]);
                String[] items= env.getProperty(sources[i]).split(",");
                svnSource.setUrl(items[0]);
                svnSource.setUsername(items[1]);
                svnSource.setPassword(items[2]);
                svnSources[i]=svnSource;
            }

            groupMap.put(group,svnSources);
        }


        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
/*
        String username="ruixiang.ma";
        String password="3PBOD298";

        // SVNRepository repository= SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnRoot));
        //repository.setAuthenticationManager(authManager);
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        svnClientManager= SVNClientManager.newInstance(options, username, password);*/
    }

   private SVNClientManager getSVNClientManager(String username, String password){
        String key=username+" "+password;
       SVNClientManager man= manMap.get(key);
       if(man==null){
           DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
           man= SVNClientManager.newInstance(options, username, password);
           manMap.put(key,man);
       }
       return man;
   }


    public List<File> update(String group,SvnSource svnSource) throws SVNException {

        SVNURL svnURL=SVNURL.parseURIEncoded(svnSource.getUrl());// "svn://192.168.0.19/common/branches/test";
        SVNClientManager man=getSVNClientManager(svnSource.getUsername(),svnSource.getPassword());
        File savePath=new File(env.getProperty("saveRoot"),group);
        savePath=new File(savePath,svnSource.getName());
        savePath.mkdirs();
        List<File> result=new ArrayList<>();
        man.setEventHandler(new ISVNEventHandler() {
            @Override
            public void handleEvent(SVNEvent event, double progress) throws SVNException {
                //update_delete  update_add update_update
                File file=event.getFile();
                if(file==null || file.isDirectory()) return;
                if(!file.getName().endsWith(".java")) return;
                if(event.getAction().equals(SVNEventAction.UPDATE_ADD) || event.getAction().equals(SVNEventAction.UPDATE_UPDATE)){
                    result.add(file);
                    logger.info(event+" ... "+progress);
                }


                //if(event.getFile()!=null && event.getFile().isFile())
                //
                //event.getAction().equals(SVNEventAction.UPDATE_ADD)

            }

            @Override
            public void checkCancelled() throws SVNCancelException {

            }
        });
       /* if(savePath.exists()) {
           //   man.getAdminClient().doListLocks(savePath);
            man.getAdminClient().doRemoveLocks(new File(savePath,".svn"), new String[]{""});
        }*/
        /*svnClientManager.getUpdateClient().doUpdate(  new File("/data/svn"),
                SVNRevision.HEAD, SVNDepth.INFINITY, true,true   );*/
        man.getUpdateClient().doCheckout( svnURL,
                savePath, SVNRevision.HEAD,SVNRevision.HEAD, SVNDepth.INFINITY,true);
        System.out.println("end");
        return result;
    }
}
