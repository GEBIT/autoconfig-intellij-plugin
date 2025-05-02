package data

import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.GitHubProject
import com.intellij.ide.starter.project.GitProjectInfo
import com.intellij.ide.starter.project.TestCaseTemplate

class Context(ideName: String, repositoryUrl: String, branch: String) {

    val ideInfo = when (ideName) {
        "PY" -> IdeProductProvider.PY;
        "WS" -> IdeProductProvider.WS;
        "IC" -> IdeProductProvider.IC;
        else -> IdeProductProvider.IC;
    }

    val repoDetails: GitProjectInfo = GitHubProject.fromGithub(
        branchName = branch,
        repoRelativeUrl = repositoryUrl
    )

    val context: TestCase<GitProjectInfo> = ContextTemplate(ideInfo).withProject(repoDetails)
}

class ContextTemplate(ideInfo: IdeInfo) : TestCaseTemplate(ideInfo)