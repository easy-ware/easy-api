package com.github.easyware.easyapiapp.svn;

import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc2.SvnCleanup;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.File;

public class SvnTest {
    SVNClientManager svnClientManager;
    String svnRoot="svn://192.168.0.19/common/branches/test";
    @Before
    public void before() throws SVNException {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();


        String username="ruixiang.ma";
        String password="3PBOD298";

       // SVNRepository repository= SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnRoot));
        //repository.setAuthenticationManager(authManager);
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        svnClientManager= SVNClientManager.newInstance(options, username, password);
    }
    @Test
    public void test() throws SVNException {
        svnClientManager.setEventHandler(new ISVNEventHandler() {
            @Override
            public void handleEvent(SVNEvent event, double progress) throws SVNException {
              //if(event.getFile()!=null && event.getFile().isFile())
               // if(event.getFile()!=null && event.getFile().isDirectory()) return;
                //event.getAction().equals(SVNEventAction.UPDATE_ADD)
                System.out.println(event+" ... "+progress);
            }

            @Override
            public void checkCancelled() throws SVNCancelException {

            }
        });
    //update_delete  update_add update_update
        /*svnClientManager.getUpdateClient().doUpdate(  new File("/data/svn"),
                SVNRevision.HEAD, SVNDepth.INFINITY, true,true   );*/
        File savePath=new File("/data/svn","group1");
        savePath=new File(savePath,"source1");
        savePath.mkdirs();
        //man.getAdminClient().doRemoveLocks(new File(savePath,".svn"), new String[]{""});
        svnClientManager.getUpdateClient().doCheckout( SVNURL.parseURIEncoded(svnRoot ),
                new File("/data/svn"), SVNRevision.HEAD,SVNRevision.HEAD, SVNDepth.INFINITY,true);
        System.out.println("end");
    }

    @Test
    public void test2() throws SVNException {
        SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
       // svnOperationFactory.setAuthenticationManager(repo.getAuthenticationManager());
       // svnOperationFactory.setOptions(new DefaultSVNOptions());
        SvnCleanup svnCleanup=  svnOperationFactory.createCleanup();
        svnCleanup.setBreakLocks(true);
        svnCleanup.addTarget(SvnTarget.fromFile( new File("/data/openapi/group1/s11")));
        //SvnCommit commit = svnOperationFactory.createCommit();
        //commit.addTarget(SvnTarget.fromFile(new File("D:/SVN/Temp/"+tmpPath)));
        svnCleanup.run();
    }
}
