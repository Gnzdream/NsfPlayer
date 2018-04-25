package com.zdream.famitracker.sound.emulation.expansion.external;

import com.zdream.famitracker.sound.emulation.expansion.external.Emu2413Context.OPLL_PATCH;
import com.zdream.famitracker.sound.emulation.expansion.external.Emu2413Context.OPLL_SLOT;

public class OPLL {

	/**
	 * unsigned
	 */
	int adr;
	int out;

	/**
	 * unsigned
	 */
	int realstep;

	/**
	 * unsigned
	 */
	int oplltime;

	/**
	 * unsigned
	 */
	int opllstep;
	int prev, next;
	int[] sprev = new int[2], snext = new int[2];

	/**
	 * unsigned
	 */
	int[] pan = new int[16];

	/* Register */

	/**
	 * unsigned
	 */
	byte[] reg = new byte[0x40];
	int[] slot_on_flag = new int[18];

	/* Pitch Modulator */

	/**
	 * unsigned
	 */
	int pm_phase;
	int lfo_pm;

	/* Amp Modulator */
	int am_phase;
	int lfo_am;

	/**
	 * unsigned
	 */
	int quality;

	/**
	 * Noise Generator, unsigned
	 */
	int noise_seed;

	/* Channel Data */
	int[] patch_number = new int[9];
	int[] key_status = new int[9];

	/* Slot */
	OPLL_SLOT[] slot = new OPLL_SLOT[18];

	/* Voice Data */
	OPLL_PATCH[] patch = new OPLL_PATCH[19 * 2];

	/**
	 * flag for check patch update
	 */
	int[] patch_update = new int[2];

	/**
	 * unsigned
	 */
	int mask;
	
	/*public OPLL() {
		  maketables (clk, rate);

		  opll = (OPLL *) calloc (sizeof (OPLL), 1);
		  if (opll == NULL)
		    return NULL;

		  for (i = 0; i < 19 * 2; i++)
		    memcpy(&opll->patch[i],&null_patch,sizeof(OPLL_PATCH));

		  opll->mask = 0;

		  OPLL_reset (opll);
		  OPLL_reset_patch (opll, 0);

		  return opll;
	}
	
	 Create Object 
	EMU2413_API OPLL *OPLL_new(uint32 clk, uint32 rate) ;
	EMU2413_API void OPLL_delete(OPLL *) ;

	 Setup 
	EMU2413_API void OPLL_reset(OPLL *) ;
	EMU2413_API void OPLL_reset_patch(OPLL *, int32) ;
	EMU2413_API void OPLL_set_rate(OPLL *opll, uint32 r) ;
	EMU2413_API void OPLL_set_quality(OPLL *opll, uint32 q) ;
	EMU2413_API void OPLL_set_pan(OPLL *, uint32 ch, uint32 pan);

	 Port/Register access 
	EMU2413_API void OPLL_writeIO(OPLL *, uint32 reg, uint32 val) ;
	EMU2413_API void OPLL_writeReg(OPLL *, uint32 reg, uint32 val) ;

	 Synthsize 
	EMU2413_API int16 OPLL_calc(OPLL *) ;
	EMU2413_API void OPLL_calc_stereo(OPLL *, int32 out[2]) ;

	 Misc 
	EMU2413_API void OPLL_setPatch(OPLL *, const uint8 *dump) ;
	EMU2413_API void OPLL_copyPatch(OPLL *, int32, OPLL_PATCH *) ;
	EMU2413_API void OPLL_forceRefresh(OPLL *) ;
	 Utility 
	EMU2413_API void OPLL_dump2patch(const uint8 *dump, OPLL_PATCH *patch) ;
	EMU2413_API void OPLL_patch2dump(const OPLL_PATCH *patch, uint8 *dump) ;
	EMU2413_API void OPLL_getDefaultPatch(int32 type, int32 num, OPLL_PATCH *) ;

	 Channel Mask 
	EMU2413_API uint32 OPLL_setMask(OPLL *, uint32 mask) ;
	EMU2413_API uint32 OPLL_toggleMask(OPLL *, uint32 mask) ;

	int32 OPLL_getchanvol(int i);*/
}
