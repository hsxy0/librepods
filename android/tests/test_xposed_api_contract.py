from pathlib import Path
import unittest


ANDROID = Path(__file__).resolve().parents[1]
MODULE = (
    ANDROID
    / "app/src/main/java/me/kavishdevar/librepods/utils/KotlinModule.kt"
)


class XposedApiContractTest(unittest.TestCase):
    def test_api_100_is_compiled_locally_but_not_packaged(self):
        app_build = (ANDROID / "app/build.gradle.kts").read_text()
        settings = (ANDROID / "settings.gradle.kts").read_text()
        self.assertIn('include(":xposed-api")', settings)
        self.assertIn('compileOnly(project(":xposed-api"))', app_build)
        self.assertNotIn('libs.libxposed.api', app_build)

    def test_entry_and_hook_match_api_100_contract(self):
        module = MODULE.read_text()
        api_module = (
            ANDROID
            / "xposed-api/src/main/java/io/github/libxposed/api/XposedModule.java"
        ).read_text()
        api_interface = (
            ANDROID
            / "xposed-api/src/main/java/io/github/libxposed/api/XposedInterface.java"
        ).read_text()
        self.assertIn('XposedModule(@NonNull XposedInterface base', api_module)
        self.assertIn('XposedModule(base, param)', module)
        self.assertIn('hook(updateIconMethod, BluetoothIconHooker::class.java)', module)
        self.assertIn('fun before(callback: XposedInterface.BeforeHookCallback)', module)
        self.assertIn('fun after(callback: XposedInterface.AfterHookCallback)', module)
        self.assertIn('void returnAndSkip(@Nullable Object result)', api_interface)
        self.assertIn('callback.returnAndSkip(null)', module)


if __name__ == "__main__":
    unittest.main()
