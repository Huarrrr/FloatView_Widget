## How to

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```css
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

//gradle 7.0+  setting.gradle

dependencyResolutionManagement {
   	...
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

```

**Step 2.** Add the dependency

```css
	dependencies {
	        implementation 'com.github.Huarrrr:FloatView_Widget:1.0.0'
	}
```
