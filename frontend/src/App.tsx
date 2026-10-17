import { MotionConfig } from 'motion/react'
import { Hero } from './sections/Hero'
import { HowItWorks } from './sections/HowItWorks'
import { Perks } from './sections/Perks'
import { Roles } from './sections/Roles'
import { Closing } from './sections/Closing'

export default function App() {
  return (
    <MotionConfig reducedMotion="user">
      <main>
        <Hero />
        <HowItWorks />
        <Perks />
        <Roles />
        <Closing />
      </main>
    </MotionConfig>
  )
}
