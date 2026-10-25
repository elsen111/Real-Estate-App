import type { PropertyShowcaseSubsectionContent } from "../../../types/content/siteContentTypes"


const SubsectionHeader = ({title, tagline=''} : PropertyShowcaseSubsectionContent) => {
  return (
      <header className="mb-12">
            <h5 className="text-5xl sm:text-start text-center text-secondary font-bold tracking-tight leading-15">{title}</h5>
            {tagline && <p className="mt-2 text-[1.6rem] text-center sm:text-start leading-relaxed text-gray-400 font-medium"> {tagline} </p>}
      </header>
  )
}

export default SubsectionHeader