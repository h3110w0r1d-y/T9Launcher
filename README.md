<div align="center">
  <img src="./image/icon.png" alt="T9启动器" width="128" height="128">
  <h1>T9启动器</h1>

[![GitHub release](https://img.shields.io/github/release/h3110w0r1d-y/T9Launcher.svg)](https://github.com/h3110w0r1d-y/T9Launcher/releases/latest)
[![main](https://github.com/h3110w0r1d-y/T9Launcher/actions/workflows/release.yml/badge.svg)](https://github.com/h3110w0r1d-y/T9Launcher/actions/workflows/release.yml)
![Android](https://img.shields.io/badge/Android-8.0%2B-blue)
![API](https://img.shields.io/badge/API-26%2B-green)
![License: CC BY-NC-SA 4.0](https://img.shields.io/badge/License-CC_BY--NC--SA_4.0-yellow.svg)
![GitHub stars](https://img.shields.io/github/stars/h3110w0r1d-y/T9Launcher?style=social)
</div>

---

## 技术栈

- Kotlin
- Jetpack Compose UI

## 项目背景

习惯了氢OS的上划九键搜索，换到 ColorOS 后虽然可以下划搜索，但是不带九键键盘、内容冗杂、体验很差。其他替代品大多不支持全拼，搜索速度慢，所以自己造一个。

## 主要功能

- 支持多音字
- 支持首字母+全拼混输
- 支持英文分词
- 隐藏系统应用
- 隐藏自定义应用
- 自定义界面样式

## 使用说明

- 长按`⚙️`进入设置界面
- 点击`⌫`删除一个字符，长按`⌫`清空所有输入
- 长按任意数字显示隐藏应用

## 搜索示例

- `冰箱IceBox` → `bxib`（支持大写首字母分词）
- `Bambu Handy` → `bh`（支持空格分词）
- `高德地图` → `gaodeditu`（支持全拼）
- `高德地图` → `gddt`（支持首字母）
- `高德地图` → `gdedt`（支持首字母+全拼混合）
- `高德地图` → `ditu`（支持从中间开始搜索）
- `QQ邮箱` → `qqyo`（支持最后一个字拼一半）
- `1DM+` → `1dm0`（支持特殊字符，用0表示）

## 许可证
本项目使用 CC BY-NC-SA 4.0 许可证。

如有建议或问题欢迎提交 Issue 反馈。
