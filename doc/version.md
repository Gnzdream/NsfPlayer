
## 版本更新历史

#### v0.2.3

（2018-10-22）

*	（FTM）支持 VRC6 和 MMC5 芯片音轨；
*	（Mixer）补充 XgmSoundMixer 混音器，以及相关的拦截器（效果器）；

#### v0.2.4

（2018-10-29）

*	（NSF）重构了 Nsf 部分的渲染方式，现在采用的 NsfRender 类的工作方式与 FamiTrackerRender 的工作方式相同；
*	（NSF）支持 2A03、2A05、VRC6 和 MMC5 芯片音轨；

（2018-10-31）

*	（NSF）支持 FDS 芯片音轨；
*	（FTM）支持 FDS 芯片音轨；
*	（Mixer）支持 FDS 芯片类型的音频播放；

（2018-11-01）

*	（FTM）支持 FDS 的 FamiTracker 效果；

#### v0.2.5

（2018-11-03）

*	（FTM）支持读取 FTM 生成的 TXT 文件，使用该文件生成 FtmAudio。支持的芯片有 2A03、2A05、VRC6、MMC5 和 FDS；
*	（FTM）统一了在生成 FtmAudio 时，出现的格式错误的异常消息反馈；
*	（BUG 修复）（FTM）修复了 Gxx 延迟效果在新一个 Gxx 效果出现时产生的问题；
*	（BUG 修复）（FTM）修复了序列中的琶音（ARPEGGIO）在相对方式（RELATIVE）时产生的问题；

#### v0.2.6

（2018-11-06）

*	（NSF）支持 N163 芯片音轨；
*	（FTM）支持 N163 芯片音轨，支持加载含 N163 的 FTM 文件；
*	（Mixer）支持 N163 芯片类型的音频播放；
*	（BUG 修复）（FTM）修复了 3xx 在 300 效果触发后修改音高状态仍然存在的 BUG；

（2018-11-07）

*	（FTM）支持加载含 N163 的 TXT 文件；

#### v0.2.7

（2018-11-11）

*	（NSF）支持 VRC7 芯片音轨；
*	（Mixer）支持 VRC7 芯片类型的音频播放；

（2018-11-13）

*	（FTM）支持 VRC7 芯片音轨，支持加载含 VRC7 的 FTM 文件；

（2018-11-14）

*	（FTM）支持加载含 VRC7 的 TXT 文件；

#### v0.2.8

（2018-11-17）

*	（NSF）支持 S5B 芯片音轨；
*	（Mixer）支持 S5B 芯片类型的音频播放；
*	（BUG 修复）（NSF）（Mixer）修复了含 Envelope 部分 Noise 轨道无法发出声音的 BUG；
