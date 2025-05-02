package de.gebit.plugins.autoconfig.builder

import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.junit5.getName
import com.intellij.ide.starter.runner.CurrentTestMethod
import com.intellij.ide.starter.runner.Starter
import data.Context


class TestContextBuilder {
    private lateinit var ide: String;
    private lateinit var repo: String;
    private lateinit var branch: String;
    private lateinit var ideVersion: String;


    fun withIde(ide: String): TestContextBuilder {
        this.ide = ide;
        return this;
    }

    fun withRepository(repo: String): TestContextBuilder {
        this.repo = repo;
        return this;
    }

    fun withBranch(branch: String): TestContextBuilder {
        this.branch = branch;
        return this;
    }

    fun withIDEVersion(ideVersion: String): TestContextBuilder {
        this.ideVersion = ideVersion;
        return this;
    }

    fun build(): IDETestContext {

        val context = Context(ide, repo, branch);

        var testCase = context.context

        if (ideVersion == "EAP") {
            testCase = testCase.useEAP()
        } else {
            testCase = testCase.withVersion(ideVersion)
        }

        return Starter.newContext(CurrentTestMethod.getName(), testCase)
            .prepareProjectCleanImport()
    }
}