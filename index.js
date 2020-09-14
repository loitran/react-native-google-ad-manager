import { NativeModules } from 'react-native'
import { RNGAMBanner } from './src/RNGAMBanner'
import { GAMAutomaticBanner } from './src/GAMAutomaticBanner'

const noop = () => {}

export { GAMAutomaticBanner }
export { RNGAMBanner }

export default { GAMAutomaticBanner, RNGAMBanner }
