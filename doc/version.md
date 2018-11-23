
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
*	（BUG 修复）（Mixer）修复了 Noise 轨道由于采样错误导致轨道音偏大的 BUG；
	该问题只在 Xgm 混音器中出现；
*	（BUG 修复）（NSF）修复了 NsfRenderer 在 ready(NsfAudio) 方法启动时无法渲染的 BUG；
*	（BUG 修复）（NSF）修复了如果使用 Bank 扩充 ROM，DPCM 轨道无法获取采样数据的 BUG；

（2018-11-18）

*	（NSF）现在 NSF 部分也支持 Blip 混音器了；
*	（Mixer）Blip 混音器支持播放 S5B 轨道；

#### v0.2.9

（2018-11-21）

*	（FTM）补充了改变播放速度的功能；

（2018-11-22）

*	（NSF）补充了改变播放速度的功能；

（2018-11-23）

*	（FTM）（NSF）补充了渲染器的输出 short 采样数组的接口；
*	（FTM）（NSF）补充了跳帧 Skip 功能；
*	（FTM）补充了重置播放位置 switchTo 功能，以及获取播放下一行位置等功能；

