import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
}

repositories {
  mavenCentral()
  google()

  maven {
    url = uri("https://maven.d4science.org/nexus/content/repositories/dnet-deps/")
  }

  maven {
    url = uri("https://repo.osgeo.org/repository/geotools-releases/")
  }
}

kotlin {
  jvm()

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutinesSwing)

      implementation("org.jdesktop:appframework:1.0.3")
      implementation("org.jdesktop:beansbinding:1.2.1")
      implementation("org.apache.commons:commons-lang3:3.8.1")
      implementation("jargs:jargs:1.0")
      implementation("net.java.dev.jna:jna:5.9.0")
      implementation("net.java.dev.jna:jna-platform:5.9.0")
      implementation("org.scream3r:jssc:2.8.0")
      implementation("org.apache.logging.log4j:log4j-api:2.12.4")
      implementation("org.apache.logging.log4j:log4j-core:2.12.4")
      implementation("com.neuronrobotics:nrjavaserial:5.2.1")
      implementation("org.jdesktop:swing-layout:1.0.2")
      implementation("org.jdesktop:swing-worker:1.1")
      implementation("xerces:xercesImpl:2.12.0")
      implementation("xml-apis:xml-apis:2.0.2")

      implementation(
        files(
          "lib/CMDFW.jar",
          "lib/DLRFIDLibrary.jar",
          "lib/JavaPOS.jar",
          "lib/JavaPOSTest.jar",
          "lib/jcl.jar",
          "lib/jpos114.jar",
          "lib/jpos114-controls.jar",
          "lib/jpos-dls-ext.jar",
          "lib/jsr80.jar",
          "lib/jsr80-ri.jar",
          "lib/jsr80_linux.jar",
        )
      )
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.chad.pos.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.chad.pos"
      packageVersion = "1.0.0"
    }
  }
}
