package com.github.easyware.easyapiapp;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.HttpConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitUtil {
	//private static Log log = LogFactory.getLog(GitUtil.class);

	private GitUtil() {
	}

	public static Git getGit(String uri, CredentialsProvider credentialsProvider, String localDir) throws Exception {
		Git git = null;
		if (new File(localDir).exists() ) {
			git = Git.open(new File(localDir));
		} else {
			git = Git.cloneRepository().setCredentialsProvider(credentialsProvider).setURI(uri)
					.setDirectory(new File(localDir)).call();
		}
		//设置一下post内存，否则可能会报错Error writing request body to server
		git.getRepository().getConfig().setInt(HttpConfig.HTTP, null, HttpConfig.POST_BUFFER_KEY, 512*1024*1024);
		return git;
	}

	public static CredentialsProvider getCredentialsProvider(String username, String password) {
		return new UsernamePasswordCredentialsProvider(username, password);
	}

	public static Repository getRepository(Git git) {
		return git.getRepository();
	}

	public static void pull(Git git, CredentialsProvider credentialsProvider) throws Exception {
		git.pull().setRemote("origin").setCredentialsProvider(credentialsProvider).call();
	}

	public static void push(Git git, CredentialsProvider credentialsProvider, String filepattern, String message)
			throws Exception {

		git.add().addFilepattern(filepattern).call();
		git.add().setUpdate(true);
		git.commit().setMessage(message).call();
		git.push().setCredentialsProvider(credentialsProvider).call();

	}

	public static void main(String[] args) throws Exception {
		String uri = "http://127.0.0.1:3000/XXX/git_test.git";
		String username = "XXX";
		String password = "123456";
		CredentialsProvider credentialsProvider = getCredentialsProvider(username, password);

		String localDir = "D:/tmp/git_test";
		Git git = getGit(uri, credentialsProvider, localDir);
		pull(git, credentialsProvider);
		push(git, credentialsProvider, ".", "提交文件");

	}

}